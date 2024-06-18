package input;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Reads a stream of StreamItems from a file.
 */
public class StreamEdgeReader {
	private BufferedReader in;
	private String sep;

	public StreamEdgeReader(BufferedReader input, String sep) {
		this.in = input;
		this.sep = sep;
	}

	public StreamEdge nextItem() throws IOException {
		String line = null;
		try {
			line = in.readLine();
			//line = line.replace(",", " ");//自己加的
            //System.out.println("-------------"+line);
			if (line == null || line.length() == 0 )
				return null;

			if (line.startsWith("#"))
				return null;
			//System.out.println("-------------"+line);


            //line=line.replace(/\,/g,"m");
			String[] tokens = line.split(" ");
			//System.out.println("-------------"+line);
			if (tokens.length < 4)//要改
				return null;
			//System.out.println("-------------"+line);//问题
			int src = Integer.parseInt(tokens[0]);
			int srcLabel = Integer.parseInt(tokens[2]);
			int dest = Integer.parseInt(tokens[1]);
			int dstLabel = Integer.parseInt(tokens[3]);

			return new StreamEdge(src, srcLabel, dest, dstLabel);

			} catch(IOException e){
				System.err.println("Unable to read from file");
				throw e;
			}

		}

}
