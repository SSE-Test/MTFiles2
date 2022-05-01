import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TaintBenchConverter {

	public static void main(String[] args) {
		String databaseName = "TB";
		List<String>lineList = ReadFile("D:\\Documents\\Uni\\21-22_Winter\\SSE Seminar\\Paper\\"+databaseName+"Sinks.txt");
		Stringset[] result = formatElements(splitUpLines(lineList));
		for(Stringset s: result) {
			s.print();
		}
		List<String> sinks = assembleStringsets(result,"SINK");
		List<String> sources = assembleStringsets(result,"SOURCE");
		
		String sinkFile = createModelString(sinks, "SINK", databaseName);
		String sourceFile = createModelString(sources, "SOURCE", databaseName);
		
		BufferedWriter writerSink;
		try {
			writerSink = new BufferedWriter(new FileWriter("D:\\Documents\\Uni\\21-22_Winter\\SSE Seminar\\Paper\\"+databaseName+"SinkModel.txt"));
		    writerSink.write(sinkFile);
		    writerSink.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		BufferedWriter writerSource;
		try {
			writerSource = new BufferedWriter(new FileWriter("D:\\Documents\\Uni\\21-22_Winter\\SSE Seminar\\Paper\\"+databaseName+"SourceModel.txt"));
		    writerSource.write(sourceFile);
		    writerSource.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//String[] a = {"java.lang.String","byte","org.springframework.http.HttpEntity", "java.lang.Class","java.lang.Object[][]"};
		//Stringset s= new Stringset("org.springframework.web.client.RestTemplate", 
		//		"org.springframework.http.ResponseEntity", "exchange", a, "SINK");
		//Stringset[] set = {s};
		//formatElements(set)[0].print();
		
	}

	private static List<String> ReadFile(String filename) {
		BufferedReader fileReader = null;

		String line = "";
		List<String> tokens = new ArrayList<String>();

		try {
			fileReader = new BufferedReader(new FileReader(filename));
			while ((line = fileReader.readLine()) != null) {
				if (line.length() > 1) {
					tokens.add(line);
				}
			}
			fileReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tokens;
	}

	private static Stringset[] splitUpLines(List<String> linesList) {
		Stringset[] partitionedStrings = new Stringset[linesList.size()]; // form: class, returntype, method name, variable, sink/source

		for (int i = 0; i < linesList.size(); i++) {
			String s = linesList.get(i);

			partitionedStrings[i] = new Stringset();
			String withoutILT = s.replaceFirst("<", "");
			String[] classnameSplit = withoutILT.split(": "); // split of the class in front
			partitionedStrings[i].classname = classnameSplit[0]; // class name

			String[] returnAndMethodNameSplit = classnameSplit[1].split("\\(");
			String returnTypeAndMethodName = returnAndMethodNameSplit[0];// return Type and method name
			String paramAndSrcSink = returnAndMethodNameSplit[1]; // method parameter and source/sink

			String[] splitRetAndMetName = returnTypeAndMethodName.split(" ");
			partitionedStrings[i].returntype = splitRetAndMetName[0]; // return type
			partitionedStrings[i].methodname = splitRetAndMetName[1]; // method name

			String[] paramAndSrcSinkSplit = paramAndSrcSink.split("\\)> -> \\_");
			partitionedStrings[i].parameters = paramAndSrcSinkSplit[0].split(","); //parameter(s)
			partitionedStrings[i].sinkSrc = paramAndSrcSinkSplit[1].replace("_", ""); //sink/source

			//System.out.println(s);
			//partitionedStrings[i].print();
			//System.out.println(partitionedStrings[i][0] + "____" + partitionedStrings[i][1] + "____"
			//		+ partitionedStrings[i][2] + "____" + partitionedStrings[i][3] + "____" + partitionedStrings[i][4]);
		}
		return partitionedStrings;
	}
	
	private static Stringset[] formatElements(Stringset[] list){
		Stringset[] formattedElements = new Stringset[list.length];
		for(int i = 0; i<list.length; i++) {
			list[i].print();
			formattedElements[i] = new Stringset();
			for (int j = 0; j<list[i].length; j++) {
				switch (j) {
					case 0: // classname
						formattedElements[i].classname = checkJVM(list[i].classname);
						break;
					case 1: // returntype
						formattedElements[i].returntype = checkJVM(list[i].returntype);
						//set.returntype = "L"+set.returntype;
						break;
					case 2: // methodname
						formattedElements[i].methodname = list[i].methodname;
						break;
					case 3: //parameters
						formattedElements[i].parameters = new String[list[i].parameters.length];
						for (int k = 0; k<list[i].parameters.length; k++) {
							formattedElements[i].parameters[k] = checkJVM(list[i].parameters[k]); 
						}
						break;
					case 4: //sink/source
						formattedElements[i].sinkSrc = list[i].sinkSrc;
						break;
					default:
						break;
				}
			}
			//formattedElements[i].print();
		}
		return formattedElements;
	}
	
	private static String checkJVM(String s) {
		String result = s;
		int arrayDimension = 0;
		
		while(true) {								//measure array depth, remove []'s
			String tmpResult = result.replaceFirst("\\[\\]", "");
			if (!tmpResult.equals(result)) {
				result = tmpResult;
				arrayDimension++;			
			}
			else {
				break;
			}
		}

		switch (s) {				//check for specified types or append "L"
		case "byte":
			result = "B";
			break;
		case "char":
			result = "C";
			break;
		case "double":
			result = "D";
			break;
		case "float":
			result = "F";
			break;
		case "int":
			result = "I";
			break;
		case "long":
			result = "J";
			break;
		case "short":
			result = "S";
			break;
		case "void":
			result = "V";
			break;
		case "boolean":
			result= "Z";
			break;
		case "": //when we have no parameters
			result = "";
			break;
		default:
			result = "L"+result;
			break;
		}
		
		for (int i = 0; i<arrayDimension; i++) {  //add array depth back to result
			result = "\\\\["+result;
		}
		result = result.replaceAll("\\.", "/");
		return result;
	}
	
	//transforms stringset lists into a list of string elements for a source or sink model
	private static List<String> assembleStringsets(Stringset[] set, String srcSink) {
		List<String> strings = new ArrayList<String>();
		for(int i = 0; i<set.length; i++)
		{	
			if(set[i].sinkSrc.equals(srcSink)) 
			{
				String newString = "\n   {\n"
								    +"    \"constraint\": \"signature\",\n"
						            +"    \"pattern\": \""
						            +set[i].classname+";\\\\."+ set[i].methodname + ":\\\\(";
				for (int j = 0; j<set[i].parameters.length; j++) 
				{
					if(!set[i].parameters[j].equals("")) {
						newString = newString + set[i].parameters[j] + ";";
					}
				}
				newString = newString + "\\\\)"+ set[i].returntype + ";\"\n   }";
				System.out.println(newString);
				strings.add(newString);
			}
		}
		return strings;
	}
	
	private static String createModelString(List<String> list, String type, String nameStart) {
		String result = "";
		
		switch(type) {
			case "SINK":
				result ="{\r\n"
						+ "  \"model_generators\": [\r\n"
						+ "    {\r\n"
						+ "      \"find\": \"methods\",\r\n"
						+ "      \"where\": [\r\n"
						+ "        {\r\n"
						+ "          \"constraint\": \"any_of\",\r\n"
						+ "          \"inners\": [";
				for (int i = 0; i<list.size()-1; i++) {
					result = result+list.get(i)+",";
				}
				result = result+list.get(list.size()-1);
				result = result + "\n               	     ],\r\n"
						+ "      	     }\r\n"
						+ "      ],\r\n"
						+ "      \"model\": {\r\n"
						+ "        \"for_all_parameters\": [\r\n"
						+ "          {\r\n"
						+ "            \"variable\": \"x\",\r\n"
						+ "            \"where\": [],\r\n"
						+ "            \"sinks\": [\r\n"
						+ "              {\r\n"
						+ "                \"kind\": \""+nameStart+"SinkModel\",\r\n"
						+ "                \"port\": \"Argument(x)\"\r\n"
						+ "              }\r\n"
						+ "            ]\r\n"
						+ "          }\r\n"
						+ "        ]\r\n"
						+ "      }\r\n"
						+ "    }\r\n"
						+ "  ]\r\n"
						+ "}";
				break;
			case "SOURCE":
				result ="{\r\n"
						+ "  \"model_generators\": [\r\n"
						+ "    {\r\n"
						+ "      \"find\": \"methods\",\r\n"
						+ "      \"where\": [\r\n"
						+ "        {\r\n"
						+ "          \"constraint\": \"any_of\",\r\n"
						+ "          \"inners\": [";
				for (int i = 0; i<list.size()-1; i++) {
					result = result+list.get(i)+",";
				}
				result = result+list.get(list.size()-1);
				result = result + "\n				],\r\n"
						+ "      	     }\r\n"
						+ "      ],\r\n"
						+ "      \"model\": {\r\n"
						+ "        \"for_all_parameters\": [\r\n"
						+ "          {\r\n"
						+ "            \"variable\": \"x\",\r\n"
						+ "            \"where\": [],\r\n"
						+ "            \"sources\": [\r\n"
						+ "              {\r\n"
						+ "                \"kind\": \""+nameStart+"SourceModel\",\r\n"
						+ "                \"port\": \"Argument(x)\"\r\n"
						+ "              }\r\n"
						+ "            ]\r\n"
						+ "          }\r\n"
						+ "        ]\r\n"
						+ "      }\r\n"
						+ "    }\r\n"
						+ "  ]\r\n"
						+ "}";
				break;
			default:
				break;
		}
		return result;
	}
}

/*
{
  "model_generators": [
    {
      "find": "methods",
      "where": [
        {
          "constraint": "any_of",
          "inners": [
          
          ...
          
                	     ],
      	     }
      ],
      "model": {
        "for_all_parameters": [
          {
            "variable": "x",
            "where": [],
            "sinks": [
              {
                "kind": "UltraSink",
                "port": "Argument(x)"
              }
            ]
          }
        ]
      }
    }
  ]
}




{
  "model_generators": [
    {
      "find": "methods",
      "where": [
        {
          "constraint": "any_of",
          "inners": [

...

				],
      	     }
      ],
      "model": {
        "for_all_parameters": [
          {
            "variable": "x",
            "where": [],
            "sources": [
              {
                "kind": "UltraSource",
                "port": "Argument(x)"
              }
            ]
          }
        ]
      }
    }
  ]
}

*/