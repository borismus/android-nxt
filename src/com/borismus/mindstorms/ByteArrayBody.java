package com.borismus.mindstorms;

import java.io.IOException;

import java.io.OutputStream;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.entity.mime.MIME;

/**
 * Byte array container for MultipartEntity
 * @author http://code.google.com/p/statusinator
 *
 */
public class ByteArrayBody extends AbstractContentBody {

	private final byte[] bytes;
	private final String fileName;

	public ByteArrayBody(byte[] bytes, String mimeType, String fileName) {
		super(mimeType);
		this.bytes = bytes;
		this.fileName = fileName;
	}

	public String getFilename() {
		return fileName;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(bytes);
	}

	public String getCharset() {
		return null;
	}

	public long getContentLength() {
		return bytes.length;
	}

	public String getTransferEncoding() {
		return MIME.ENC_BINARY;
	}

}
