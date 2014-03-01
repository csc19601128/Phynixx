package org.csc.phynixx.xa;

public class IDGenerator implements IResourceIDGenerator {
	
	private Long current= new Long(1);

	public IDGenerator() 
	{
		this(0); 
	}

	public IDGenerator(long start) 
	{
		super();
		this.current = new Long(start);
	}
	
	public long getCurrentLong() {
		return this.current.longValue();
	}
	
	public long generateLong() {
		this.generate();
		return this.getCurrentLong();
	}
	
	
	/* (non-Javadoc)
	 * @see de.csc.xaresource.sample.IResourceIDGenerator#getCurrent()
	 */
	public Object getCurrent() 
	{
		return this.current.toString();
	}
	
	/* (non-Javadoc)
	 * @see de.csc.xaresource.sample.IResourceIDGenerator#generate()
	 */
	public Object generate() 
	{
		long cc= current.longValue();
		this.current= new Long(++cc);
		return this.getCurrent(); 		
	}
	
	

}
