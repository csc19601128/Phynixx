package org.csc.phynixx.loggersystem.messages;

class OrdinalGenerator {
	
	private Integer current= new Integer(1);

	public OrdinalGenerator() 
	{
		this(0); 
	}

	public OrdinalGenerator(int start) 
	{
		super();
		this.current = new Integer(start);
	}
	
	public int getCurrent() {
		return this.current.intValue();
	}
	
	
	/* (non-Javadoc)
	 * @see de.csc.xaresource.sample.IResourceIDGenerator#generate()
	 */
	public int generate() 
	{
		int cc= current.intValue();
		this.current= new Integer(++cc);
		return this.getCurrent(); 		
	}
	
	

}
