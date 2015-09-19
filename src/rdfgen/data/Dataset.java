package rdfgen.data;

public interface Dataset<T extends Data> {
	
	public void load();
	public void close();
	public T getNextData();
}
