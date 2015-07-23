package com.server.templete;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.FloatValue;
import org.msgpack.type.IntegerValue;
import org.msgpack.type.MapValue;
import org.msgpack.type.RawValue;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;
import org.msgpack.unpacker.Unpacker;

import com.server.util.YarUtil;

public class ObjectTemplate  extends AbstractTemplate<Object> {
	
	private Template keyTemplate;
	    
	private ObjectTemplate() {
		this.keyTemplate = Templates.TString;
	}
	
	static public ObjectTemplate getInstance() {
	    return instance;
	}

	static final ObjectTemplate instance = new ObjectTemplate();

	public void write(Packer pk, Object target, boolean required) throws IOException {
	    if (target == null) {
	        if (required) {
	            throw new MessageTypeException("Attempted to write null");
	        }
	        pk.writeNil();
	        return;
	    }
	    pk.write(target);	    	   
	}
	
	
	public Object read(Unpacker u, Object to, boolean required) throws IOException {
	    if (!required && u.trySkipNil()) {
	        return null;
	    }

	    return toObject(u.readValue());
	}

	private static Object toObject(Value value) throws IOException {
	    Converter conv = new Converter(value);
	    if (value.isNilValue()) { // null
	        return null;
	    } else if (value.isRawValue()) { // byte[] or String or maybe Date?
	        // deserialize value to String object
	        RawValue v = value.asRawValue();
	        String str = conv.read(Templates.TString);
	        return str.replaceAll("\\u0000.+\\u0000", "");
	    } else if (value.isBooleanValue()) { // boolean
	        return conv.read(Templates.TBoolean);
	    } else if (value.isIntegerValue()) { // int or long or BigInteger
	        // deserialize value to int
	        IntegerValue v = value.asIntegerValue();
	        return conv.read(Templates.TLong);
	    } else if (value.isFloatValue()) { // float or double
	        // deserialize value to double
	        FloatValue v = value.asFloatValue();
	        return conv.read(Templates.TDouble);
	    } else if (value.isArrayValue()) { // List or Set
	        // deserialize value to List object
	        ArrayValue v = value.asArrayValue();
	        List<Object> ret = new ArrayList<Object>(v.size());
	        for (Value elementValue : v) {
	            ret.add(toObject(elementValue));
	        }
	        return ret;
	    } else if (value.isMapValue()) { // Map
	        MapValue v = value.asMapValue();


	        Map map = new HashMap(v.size());
	        for (Map.Entry<Value, Value> entry : v.entrySet()) {
	            Value key = entry.getKey();
	            if(key.isNilValue()){
	            	continue;
	            }
	            Value val = entry.getValue();	            
	            map.put(toObject(key), toObject(val));
	        }

	        return map;
	    } else {
	        throw new RuntimeException("fatal error");
	    }
	}
}
