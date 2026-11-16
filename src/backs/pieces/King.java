package backs.pieces;

import backs.Color;
import backs.Piece;
import static java.lang.Math.*;

public class King extends Piece {
	public King(Color color) {
		super(color, "King", "k");
	}

	@Override
	public boolean moveIsObtainable(int[] vector, boolean onTake) {
		return abs(vector[0]) < 2
				&& abs(vector[1]) < 2;
	}

	/* Unit move, as for other long-range pieces: for castling
	*  roadClear() must check every tile between king and rook
	*/
	@Override
	public int[] getMove(int[] vector) {
		int[] answer = {(int) signum(vector[0]), 
				(int) signum(vector[1])};
		return answer;
	}
}
