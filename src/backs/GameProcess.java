package backs;

import backs.pieces.*;

public class GameProcess {
    private Board board;
    public boolean gameOver = false;
    private Color moveColor = Color.WHITE;
    public boolean isCheck = false;
    /* Tile a pawn has just jumped over with its double move.
    *  Enemy pawn may take it there ("en passant"), but only on the next move
    */
    private Pair enPassantPair = null;

    public GameProcess(Board board) {this.board = board;}

    public void move(Pair from, Pair to) throws WrongMoveExeption {
        move(from, to, 'Q');
    }

    // promotion: sign of piece for pawn on the last row (Q, R, B, K for knight)
    public void move(Pair from, Pair to, char promotion) throws WrongMoveExeption {
        Tile fromTile = board.getTileByPair(from);
        Tile toTile = board.getTileByPair(to);

        if (fromTile.isEmpty()) {
            throw new WrongMoveExeption("Empty field selected!");
        } else if (fromTile.getPiece().getColor() != moveColor) {
            throw new WrongMoveExeption("That is not your figure!");
        } else if (fromTile.pieceColorEquals(toTile)) {
            throw new WrongMoveExeption("Can't eat same colored figure!");
        } else if (moveOpensKingForCheck(from, to)) {
            throw new WrongMoveExeption("It causes check for your king!");
        } else if (isCastling(from, to)) {
            castling(from, to);
        } else if (isEnPassant(from, to)) {
            enPassant(from, to);
        } else {
            Piece fromPiece = fromTile.getPiece();
            int[] vector = fromPiece.vectorOf(from, to);
            boolean onTake = !toTile.isEmpty();
            if (!fromPiece.moveIsObtainable(vector, onTake)) {
                throw new WrongMoveExeption("That piece moves differently!");
            } else if (!board.roadClear(from, to)) {
                throw new WrongMoveExeption("Obstacle on road!");
            } else {
                board.transfer(fromTile, toTile);
                promote(toTile, promotion);
            }
        }

        rememberDoubleMove(from, to);
        changeSide();
        lookForCheck();
        checkAvailableMoves();
    }

    public boolean goodMove(Pair from, Pair to) {
        boolean answer = true;
        Tile fromTile = board.getTileByPair(from);
        Tile toTile = board.getTileByPair(to);

        if (fromTile.isEmpty()) {
            answer = false;
        } else if (fromTile.getPiece().getColor() != moveColor) {
            answer = false;
        } else if (fromTile.pieceColorEquals(toTile)) {
            answer = false;
        } else if (moveOpensKingForCheck(from, to)) {
            answer = false;
        } else if (isCastling(from, to)) {
            answer = (castlingProblem(from, to) == null);
        } else if (isEnPassant(from, to)) {
            answer = true;
        } else {
            Piece fromPiece = fromTile.getPiece();
            int[] vector = fromPiece.vectorOf(from, to);
            boolean onTake = !toTile.isEmpty();
            if (!fromPiece.moveIsObtainable(vector, onTake)) {
                answer = false;
            } else if (!board.roadClear(from, to)) {
                answer = false;
            } else {
                answer = true;
            }
        }

        return answer;
    }

    // Pawn takes diagonally to the tile, enemy pawn has just jumped over
    public boolean isEnPassant(Pair from, Pair to) {
        Piece fromPiece = board.getPieceByPair(from);

        return (fromPiece instanceof Pawn)
                && to.equals(enPassantPair)
                && fromPiece.moveIsObtainable(fromPiece.vectorOf(from, to), true);
    }

    public void enPassant(Pair from, Pair to) {
        // Taken pawn stands beside, not on the target tile
        board.remove(board.getTileByPair(new Pair(from.i(), to.j())));
        board.transfer(from, to);
    }

    private void rememberDoubleMove(Pair from, Pair to) {
        if ((board.getPieceByPair(to) instanceof Pawn)
                && Math.abs(to.i() - from.i()) == 2) {
            enPassantPair = new Pair((from.i() + to.i()) / 2, from.j());
        } else {
            enPassantPair = null;
        }
    }

    // Pawn that reached the last row turns into another piece
    private void promote(Tile tile, char sign) {
        Piece piece = tile.getPiece();
        int lastRow = (piece.getColor() == Color.WHITE) ? 7 : 0;

        if (!(piece instanceof Pawn) || tile.getPair().i() != lastRow) {
            return;
        }

        Color color = piece.getColor();
        Piece newPiece;

        switch (sign) {
            case 'R': newPiece = new Rook(color); break;
            case 'B': newPiece = new Bishop(color); break;
            case 'K': newPiece = new Knight(color); break;
            default: newPiece = new Queen(color);
        }

        // New rook can't castle
        newPiece.firstMoveDone();
        tile.setPiece(newPiece);
    }

    public boolean notEnd() {return !gameOver;}

    private void changeSide() {
        if (moveColor == Color.WHITE) {
            moveColor = Color.BLACK;
        } else {
            moveColor = Color.WHITE;
        }
    }

    public String reason() {
        String answer;

        if (isCheck == true) {
            String winner = (moveColor == Color.WHITE) ? "BLACK" : "WHITE";
            answer = "Mate! " + winner + " wins!";
        } else {
            answer = "Pat! Draw.";
        }

        return answer;
    }

    public boolean isCastling(Pair from, Pair to) {
        boolean answer = false;
        Piece fromPiece = board.getPieceByPair(from);

        if ((to.i() == from.i())
                && (fromPiece instanceof King)
                && (fromPiece.getFirstMove())
                && (Math.abs(to.j() - from.j()) == 2)) {
            answer = true;
        } else {
            answer = false;
        }

        return answer;
    }

    public boolean tileUnderAttack(Tile tile, Color enemyColor) {
        boolean answer = false;
        TileSet arr = board.getTileSet(enemyColor);
        Tile enemyTile;
        Piece enemyPiece;

        for (int i = 0; i < arr.size(); i++) {
            enemyTile = arr.get(i);
            enemyPiece = enemyTile.getPiece();
            Pair from = enemyTile.getPair();
            Pair to = tile.getPair();
            int[] vector = enemyPiece.vectorOf(from, to);
            // Attack is a take: pawn attacks diagonally, not forward
            boolean onTake = true;

            if (enemyPiece.moveIsObtainable(vector, onTake)
                    && board.roadClear(from, to)) {
                answer = true;
                break;
            }
        }

        return answer;
    }

    public boolean tileUnderAttack(Pair pair, Color enemyColor) {
        return tileUnderAttack(board.getTileByPair(pair), enemyColor);
    }

    public boolean roadUnderAttack(Pair from, Pair to) {
        boolean answer = false;
        Piece fromPiece = board.getPieceByPair(from);
        int i = to.i() - from.i(), j = to.j() - from.j();
        Pair unitMovePair = new Pair((int) Math.signum(i),
                (int) Math.signum(j));
        Pair step = new Pair(from.i(), from.j());

        while (!step.equals(to)) {
            step = step.add(unitMovePair);
            if (tileUnderAttack(step, fromPiece.getOpponentsColor())
                    && !step.equals(to)) {
                answer = true;
                break;
            }
        }

        return answer;
    }

    // Rook of castling: on the corner of king's row, towards king's move
    private Pair rookPair(Pair fromKingPair, Pair toKingPair) {
        int j = (toKingPair.j() > fromKingPair.j()) ? 7 : 0;
        return new Pair(fromKingPair.i(), j);
    }

    /* Returns why castling is impossible, or null if it is allowed.
    *  Tile king lands on is checked by moveOpensKingForCheck()
    */
    private String castlingProblem(Pair fromKingPair, Pair toKingPair) {
        Pair fromRookPair = rookPair(fromKingPair, toKingPair);
        Piece king = board.getPieceByPair(fromKingPair);
        Piece rook = board.getPieceByPair(fromRookPair);
        String answer = null;

        if (!(rook instanceof Rook) || rook.getColor() != king.getColor()) {
            answer = "No rook for castling!";
        } else if (!rook.getFirstMove()) {
            answer = "Rook had been moved!";
        } else if (!board.roadClear(fromKingPair, fromRookPair)) {
            answer = "Obstacle on road!";
        } else if (isCheck) {
            answer = "Can't castle under check!";
        } else if (roadUnderAttack(fromKingPair, toKingPair)) {
            answer = "Road is under attack!";
        }

        return answer;
    }

    public void castling(Pair fromKingPair, Pair toKingPair) throws WrongMoveExeption {
        String problem = castlingProblem(fromKingPair, toKingPair);

        if (problem != null) {
            throw new WrongMoveExeption(problem);
        }

        // Rook stands on the tile king has passed
        Pair fromRookPair = rookPair(fromKingPair, toKingPair);
        Pair toRookPair = new Pair(fromKingPair.i(),
                (fromKingPair.j() + toKingPair.j()) / 2);
        board.transfer(fromKingPair, toKingPair);
        board.transfer(fromRookPair, toRookPair);
    }

    public boolean moveOpensKingForCheck(Pair from, Pair to) {
        boolean answer = false;
        Tile toTile = board.getTileByPair(to);
        Piece toPiece = toTile.getPiece();
        Piece fromPiece = board.getPieceByPair(from);
        // transfer() marks the piece as moved, it must be given back after the test
        boolean firstMove = fromPiece.getFirstMove();
        int toIndex = -1;
        if (!(toPiece instanceof EmptyPiece)) {
            TileSet toArr = board.getTileSet(toPiece.getColor());
            toIndex = toArr.indexOf(toTile);
        }

        // En passant also takes the pawn beside: it may cover the king
        Tile takenTile = null;
        Piece takenPiece = null;
        int takenIndex = -1;
        if (isEnPassant(from, to)) {
            takenTile = board.getTileByPair(new Pair(from.i(), to.j()));
            takenPiece = takenTile.getPiece();
            takenIndex = board.remove(takenTile);
        }

        board.transfer(from, to);

        Tile kingsTile;

        kingsTile = board.getKingsTile(moveColor);
        Piece king = kingsTile.getPiece();

        if (tileUnderAttack(kingsTile, king.getOpponentsColor())) {
            answer = true;
        }

        board.antiTransfer(from, to, toPiece, toIndex);
        if (firstMove) {
            fromPiece.firstMoveUndone();
        }
        if (takenTile != null) {
            board.restore(takenTile, takenPiece, takenIndex);
        }

        return answer;
    }

    public void lookForCheck() {
        Color enemyColor;

        if (moveColor == Color.WHITE) {
            enemyColor = Color.BLACK;
        } else {
            enemyColor = Color.WHITE;
        }

        Tile kingsTile = board.getKingsTile(moveColor);

        isCheck = tileUnderAttack(kingsTile, enemyColor);
        if (isCheck) {
            System.out.println("Check!");
        }
    }

    public void checkAvailableMoves() {
        boolean isMate = true;
        TileSet arr = board.getTileSet(moveColor);
        Pair from, to;
        boolean stop = false;

        for (int i = 0; i < arr.size() && !stop; i++) {
            from = arr.get(i).getPair();
            for (int j = 0; j < 8 && !stop; j++) {
                for (int k = 0; k < 8 && !stop; k++) {
                    to = new Pair(j, k);
                    if (goodMove(from, to)) {
                        isMate = false;
                        stop = true;
                    }
                }
            }
        }

        gameOver = isMate;
    }
}
