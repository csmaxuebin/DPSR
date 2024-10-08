package reservoir;

import struct.MapArray;
//与图模式存储一样的意思，没啥大用
public class EdgeReservoir<T> implements Reservoir<T> {
	MapArray<T> list;

	public EdgeReservoir() {
		this.list = new MapArray<T>();
	}

	@Override
	public boolean add(T a) {
		return list.add(a);
	}

	@Override
	public boolean remove(T a) {
		return list.remove(a);
	}

	@Override
	public boolean contains(T a) {
		return list.contains(a);
	}

	@Override
	public T getRandom() {
		return list.getRandom();
	}

	public int getSize() {
		return list.size();
	}
}
