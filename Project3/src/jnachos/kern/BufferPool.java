package jnachos.kern;

import java.util.HashMap;

/**
 * Buffer Pool manage the buffer which is used for communication between two
 * processes
 */
public class BufferPool {

	/**
	 * Buffer works as vehicle transferring massage between two processes
	 */
	public static class Buffer {

		/**
		 * maximum buffer size maximum message contain 50 characters
		 */
		public static final int BUFFER_SIZE = 50;

		/**
		 * identity of buffer
		 */
		private int bufferId;
		
		/**
		 * 2 types: message buffer(0), answer buffer(0)
		 */
		private int type;

		/**
		 * specify whether the buffer has been used
		 */
		private boolean used;

		/**
		 * sender name
		 */
		private String sndr_;

		/**
		 * receiver name
		 */
		private String rcvr_;

		/**
		 * content of buffer
		 */
		private String data;

		/**
		 * inside constructor, set buffer id
		 */
		public Buffer(int id) {
			bufferId = id;
			used = false;
		}

		/**
		 * get buffer id
		 */
		public int getId() {
			return bufferId;
		}
		
		/**
		 * get buffer type
		 */
		public int getType() {
			return type;
		}
		
		/**
		 * set buffer type
		 */
		public void setType(int tp) {
			type = tp;
		}

		/**
		 * get the status of the buffer for whether the buffer has been used
		 */
		public boolean isUsed() {
			return used;
		}

		/**
		 * set the status of the buffer be used
		 */
		private void setUsed(boolean temUsed) {
			used = temUsed;
		}

		/**
		 * get sender name
		 */
		public String getSender() {
			return sndr_;
		}

		/**
		 * set sender name
		 */
		public void setSender(String temSndr) {
			sndr_ = temSndr;
		}

		/**
		 * get receiver name
		 */
		public String getReceiver() {
			return rcvr_;
		}

		/**
		 * set receiver name
		 */
		public void setReceiver(String temRcvr) {
			rcvr_ = temRcvr;
		}

		/**
		 * get data of buffer
		 */
		public String getData() {
			return data;
		}

		/**
		 * set data of buffer
		 */
		public void setData(String temData) {
			assert (temData.length() <= BUFFER_SIZE);
			data = temData;
		}
	}

	/**
	 * maximum count of buffers
	 */
	public static final int BUFFER_POOL_SIZE = 1024;
	
	/**
	 * maximum count of buffers a process can apply
	 */
	public static final int maxNumOfBuf = 2;

	/**
	 * number of free buffers
	 */
	private int Free_Buffer;

	/**
	 * Buffer Pool
	 */
	private final HashMap<Integer, Buffer> buffers;

	/**
	 * initialize Buffer Pool
	 */
	public BufferPool() {
		Free_Buffer = BUFFER_POOL_SIZE;
		buffers = new HashMap<Integer, Buffer>();
		for (int i = 0; i < BUFFER_POOL_SIZE; i++) {
			Buffer buf = new Buffer(i);
			buffers.put(i, buf);
		}
	}

	/**
	 * get Buffer Pool size
	 */
	public synchronized int getPoolSize() {
		return buffers.size();
	}

	/**
	 * get a free buffer from Buffer Pool
	 */
	public synchronized Buffer getFreeBuffer() {
		while (Free_Buffer <= 0) {
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		assert (Free_Buffer > 0);

		for (int i = 0; i < Buffer.BUFFER_SIZE; i++) {
			Buffer temBuf = buffers.get(i);
			if (!temBuf.isUsed()) {
				temBuf.setUsed(true);
				Free_Buffer--;
				return temBuf;
			}
		}

		return null;
	}

	/**
	 * get a target buffer referring to corresponding buffer id
	 */
	public synchronized Buffer getBuffer(int temId) {
		return buffers.get(temId);
	}
	
	/**
	 * get a target buffer referring to corresponding buffer id
	 */
	public synchronized boolean hasBuffer(int temId) {
		return !buffers.get(temId).isUsed();
	}

	/**
	 * return buffer to Buffer Pool
	 */
	public synchronized void returnBufPool(int temId) {
		Buffer temBuf = buffers.get(temId);

		assert (temBuf.isUsed());

		temBuf.setUsed(false);
		Free_Buffer++;
		notify();
	}
}
