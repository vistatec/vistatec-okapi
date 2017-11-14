/* Generated By:JavaCC: Do not edit this line. StringProvider.java Version 6.1 */
/* JavaCCOptions:KEEP_LINE_COLUMN=true */
package net.sf.okapi.filters.json.parser;

	
	import java.io.IOException;
	
	public class StringProvider implements Provider {

		String _string;
		int _position = 0;
		int _size;
		
		public StringProvider(String string) {
			_string = string;
			_size = string.length();
		}
		
		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			int numCharsOutstandingInString = _size - _position;
			
			if (numCharsOutstandingInString == 0) {
				return -1;
			}
			
			int numBytesInBuffer = cbuf.length;
			int numBytesToRead = numBytesInBuffer -off;
			numBytesToRead = numBytesToRead > len ? len : numBytesToRead;
			
			if (numBytesToRead > numCharsOutstandingInString) {
				numBytesToRead = numCharsOutstandingInString;
			}
			
			_string.getChars(_position, _position + numBytesToRead, cbuf, off);
			
			_position += numBytesToRead;
			
			return numBytesToRead;
		}

		@Override
		public void close() throws IOException {
			_string = null;
		}
		
	}
/* JavaCC - OriginalChecksum=7c13b24ec61dea5273c66094567b2bf1 (do not edit this line) */
