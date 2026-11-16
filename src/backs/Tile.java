package backs;

import backs.pieces.EmptyPiece;

public class Tile {
    private Piece piece;
    private Color color;
    // Stores combination of indexes of this tile in "board" array
    private final Pair pair;

    // Constructor setting values of all attributes
    public Tile(Piece piece, Color color, Pair pair) {
        this.piece = piece;
        this.color = color;
        this.pair = pair;
    }

    // Setter for Piece attribute
    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    // Getter for Piece attribute
    public Piece getPiece() { return piece; }

    // Getter for Color attribute
    public Color getColor() { return color; }

    // Getter for Pair attribute
    public Pair getPair() { return pair; }
    
    // Returns name of Piece stored
    @Override
    public String toString() {
        String pieceName;

        if (piece != null) {
            pieceName = piece.toString();
        } else {
            // Name of empty piece is two spaces
        	pieceName = "  ";
        }

        if (color == Color.WHITE) {
        	return "|*" + pieceName + "*";
        } else {
        	return "| " + pieceName + " ";
        }
    }

    // Compares pieces by colors
    public boolean pieceColorEquals(Tile tile) {
        boolean answer;

        if (tile.isEmpty() || isEmpty()) {
            answer = false;
        } else if (tile.getPiece().getColor() != piece.getColor()){
            answer = false;
        } else {
            answer = true;
        }

        return answer;
    }


    // Compares by values stored in Pair attribute
    public boolean equals(Tile tile) {
        return pair.equals(tile.getPair());
    }

    /* Exempts Piece attribute. Empty tile holds EmptyPiece, not null:
    *  roadClear() and pawn moves look for EmptyPiece, so a null left
    *  after a move used to block the road like a real piece
    */
    public void clear() {this.piece = new EmptyPiece();}

    public boolean isEmpty() {return piece instanceof EmptyPiece;}
}
