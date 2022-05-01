
public class Stringset {
	String classname = "";
	String returntype = "";
	String methodname = "";
	String[] parameters = null;
	String sinkSrc = "";
	
	final public int length = 5;
	
	public Stringset(String classname, String returntype, String methodname, String[] parameters, String sinkSrc) {
		super();
		this.classname = classname;
		this.returntype = returntype;
		this.methodname = methodname;
		this.parameters = parameters;
		this.sinkSrc = sinkSrc;
	}
	
	public Stringset() {
		super();
	}
	
	public static void main(String[] arg) {
		String[] a = {"java.lang.String","org.springframework.http.HttpMethod","org.springframework.http.HttpEntity", "java.lang.Class,java.lang.Object[]"};
		Stringset s= new Stringset("org.springframework.web.client.RestTemplate", 
				"org.springframework.http.ResponseEntity", "exchange", a,
						"SINK");
		s.print();
		
		String[] test = ")> -> \\SINK_".split(",");
		for(String str:test) {
			System.out.println("Length: "+test.length+str);
		}
	}
	
	public void print() 
	{
		System.out.println(classname+"___"+returntype+"___"+methodname+"___"+arrayToString(parameters)+"___"+sinkSrc);
	}
	
	private String arrayToString(String[] array) {
		String out = "";
		for(String s:array) {
			out = out+"__"+s;
		}
		out = out.replaceFirst("\\_\\_", "");
		return out;
	}
}
