package front;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.io.IOException;
import backs.*;

public class Reader {
	private InputStreamReader isr = new InputStreamReader(System.in);
	private BufferedReader reader = new BufferedReader(isr);
	// Piece a pawn turns into on the last row: Q, R, B or K (knight)
	private char promotion = 'Q';
	
    private Pair[] format(String input) {
		StringTokenizer tokenizer;
		tokenizer = new StringTokenizer(input, "-");
        Pair[] answer = new Pair[2];
        int cnt = 0;
        
        while (tokenizer.hasMoreTokens()) {
	        String i = tokenizer.nextToken();
	        int[] arr = { i.charAt(1) - 49, i.charAt(0) - 65 };
            Pair temp = new Pair(arr);
            answer[cnt] = temp;
            cnt++;
        }
        
        return answer;
    }

    private boolean accepted(String input) {
        return input.matches("[A-H][1-8]-[A-H][1-8][QRBK]?");
    }

    // Returns null when input is over (Ctrl+D or end of file)
    public Pair[] formattedInput() {
		String input;
    	try {
    		while ((input = reader.readLine()) != null) {
    			input = input.trim().toUpperCase();
    			if (accepted(input)) {
    				promotion = (input.length() == 6) ? input.charAt(5) : 'Q';
    				return format(input);
    			}
    			System.out.println("input format MUST be 'A1-H8' (pawn promotion: 'A7-A8Q', 'A7-A8K' for knight)");
    		}
    	} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
    }

    public char getPromotion() {return promotion;}
    
    public void closeRrader() {
    	try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
