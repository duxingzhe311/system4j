package com.dw.system.util;

import java.util.LinkedList;

public class LRUBuffer {
	LinkedList<Object> buffer;

	int bufferSize = 64;

	public LRUBuffer(int size) {
		if (size > 0)
			bufferSize = size;

		buffer = new LinkedList<Object>();
	}

	public synchronized void add(long id, Object obj) {
		if (obj == null)
			return;
		buffer.addFirst(new Index(id, obj));
	}

	public synchronized Object access(long id) {
		int index = buffer.indexOf(new Index(id, null));

		if (index < 0)
			return null;

		if (index == 0)
			return buffer.getFirst();

		Index indexObject = (Index) buffer.remove(index);
		buffer.addFirst(indexObject);

		return indexObject.obj;
	}

	public synchronized void remove(long id) {

		int index = buffer.indexOf(new Index(id, null));

		if (index < 0)
			return;

		buffer.remove(index);
	}

	class Index {
		long id;
		Object obj;

		public Index(long id, Object obj) {
			this.id = id;
			this.obj = obj;
		}
	}
}