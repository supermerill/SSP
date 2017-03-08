package remi.ssp.network;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import remi.ssp.CurrentGame;

public class NetworkReflexionSave extends NetworkReflexion{

	@Override
	public String apply(String t) {
		
		//first level: raccourci
		//first level: raccourci
		Object result = decode(CurrentGame.get(), t);
		return getStringOf(result);
	}
	
	
	protected String getStringOf(Object obj){
		if(obj == null){
			return "null";
		}else{
			return getJson(obj).toString();
		}
	}
	
	protected JsonValue getJson(Object obj){
		if(obj instanceof String){
			return new JsonStringImpl(obj.toString());
		}
		if(obj instanceof SimpleSerializable){
			JsonObjectBuilder objectB = Json.createObjectBuilder();
			((SimpleSerializable) obj).save(objectB);
			return objectB.build();
		}

		if(obj instanceof Collection){
			JsonArrayBuilder arrayBuild = Json.createArrayBuilder();
			for(Object o : ((Collection<?>)obj)){
				arrayBuild.add(getJson(o));
			}
			return arrayBuild.build();
		}

		if(obj instanceof Array){
			JsonArrayBuilder arrayBuild = Json.createArrayBuilder();
			for(int i=0;i< Array.getLength(obj);i++){
				arrayBuild.add(getJson(Array.get(obj, i)));
			}
			return arrayBuild.build();
		}
		if(obj instanceof Integer){
			return new JsonLongNumber(((Integer)obj).intValue());
		}
		if(obj instanceof Long){
			return new JsonLongNumber(((Long)obj).longValue());
		}
		//TODO: copy also the doublenumber
		if(obj instanceof Float){
			return new JsonLongNumber((long)(((Float)obj).floatValue()*1000));
		}
		if(obj instanceof Double){
			return new JsonLongNumber((long)(((Double)obj).doubleValue()*1000));
		}
		
		//else
		return new JsonStringImpl(obj.toString());
	}
	
	//copy from org.json
	final static class JsonStringImpl implements JsonString {

	    private final String value;

	    JsonStringImpl(String value) {
	        this.value = value;
	    }

	    @Override
	    public String getString() {
	        return value;
	    }

	    @Override
	    public CharSequence getChars() {
	        return value;
	    }

	    @Override
	    public ValueType getValueType() {
	        return ValueType.STRING;
	    }

	    @Override
	    public int hashCode() {
	        return getString().hashCode();
	    }

	    @Override
	    public boolean equals(Object obj) {
	        if (!(obj instanceof JsonString)) {
	            return false;
	        }
	        JsonString other = (JsonString)obj;
	        return getString().equals(other.getString());
	    }

	    @Override
	    public String toString() {
	        StringBuilder sb = new StringBuilder();
	        sb.append('"');

	        for(int i = 0; i < value.length(); i++) {
	            char c = value.charAt(i);
	            // unescaped = %x20-21 | %x23-5B | %x5D-10FFFF
	            if (c >= 0x20 && c <= 0x10ffff && c != 0x22 && c != 0x5c) {
	                sb.append(c);
	            } else {
	                switch (c) {
	                    case '"':
	                    case '\\':
	                        sb.append('\\'); sb.append(c);
	                        break;
	                    case '\b':
	                        sb.append('\\'); sb.append('b');
	                        break;
	                    case '\f':
	                        sb.append('\\'); sb.append('f');
	                        break;
	                    case '\n':
	                        sb.append('\\'); sb.append('n');
	                        break;
	                    case '\r':
	                        sb.append('\\'); sb.append('r');
	                        break;
	                    case '\t':
	                        sb.append('\\'); sb.append('t');
	                        break;
	                    default:
	                        String hex = "000" + Integer.toHexString(c);
	                        sb.append("\\u").append(hex.substring(hex.length() - 4));
	                }
	            }
	        }

	        sb.append('"');
	        return sb.toString();
	    }
	}
	// Optimized JsonNumber impl for long numbers.
    private static final class JsonLongNumber implements JsonNumber {
        private final long num;
        private BigDecimal bigDecimal;  // assigning it lazily on demand

        JsonLongNumber(long num) {
            this.num = num;
        }

        @Override
        public boolean isIntegral() {
            return true;
        }

        @Override
        public long longValue() {
            return num;
        }

        @Override
        public long longValueExact() {
            return num;
        }

        @Override
        public double doubleValue() {
            return num;
        }

        @Override
        public BigDecimal bigDecimalValue() {
            // reference assignments are atomic. At the most some more temp
            // BigDecimal objects are created
            BigDecimal bd = bigDecimal;
            if (bd == null) {
                bigDecimal = bd = new BigDecimal(num);
            }
            return bd;
        }

        @Override
        public String toString() {
            return Long.toString(num);
        }

        @Override
        public BigInteger bigIntegerValue() {
            return bigDecimalValue().toBigInteger();
        }

        @Override
        public BigInteger bigIntegerValueExact() {
            return bigDecimalValue().toBigIntegerExact();
        }

        @Override
        public int intValue() {
            return bigDecimalValue().intValue();
        }

        @Override
        public int intValueExact() {
            return bigDecimalValue().intValueExact();
        }

        @Override
        public ValueType getValueType() {
            return ValueType.NUMBER;
        }
    }
}
