package com.yarclient.templete;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.unpacker.Unpacker;

import com.yarclient.bean.request.TsetBean;
import com.yarclient.util.YarUtil;
/**
 * java类的模板
 * @author wangliguang
 * @param <K,V>
 *
 */
public class JavaClassTemplete  extends AbstractTemplate<Object> {
	 private Template keyTemplate;
	 private Template valueTemplate;
	    
	private JavaClassTemplete() {
		this.keyTemplate = Templates.TString;
        this.valueTemplate = ObjectTemplate.getInstance();
	}
	public JavaClassTemplete(Template keyTemplate, Template valueTemplate) {
        this.keyTemplate = keyTemplate;
        this.valueTemplate = valueTemplate;
    }
	static public JavaClassTemplete getInstance() {
	    return instance;
	}

	static final JavaClassTemplete instance = new JavaClassTemplete();

	public void write(Packer pk, Object target, boolean required) throws IOException {
       
		Field []fields = target.getClass().getDeclaredFields();
	    Object value = null;
	    pk.writeMapBegin(fields.length);
		for(int i=0;i<fields.length;i++){
			Field field = fields[i];
			field.setAccessible(true);
			try {
				value = field.get(target);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			} 
			keyTemplate.write(pk, field.getName());
			if(YarUtil.isObject(value)){
				this.write(pk, value, required);
            }else{
            	valueTemplate.write(pk, value);		
            }			
		}
        pk.writeMapEnd();
	}
	
	public Object read(Unpacker u, Object to, boolean required)
			throws IOException {
		 if (!required && u.trySkipNil()) {
	            return null;
	        }
	        int n = u.readMapBegin();
	        Map map;
	        if (to != null) {
	            map = (Map) to;
	            map.clear();
	        } else {
	            map = new HashMap(n);
	        }
	        for (int i = 0; i < n; i++) {
	            Object key = keyTemplate.read(u, null);
	            Object value = valueTemplate.read(u, null);
	            map.put(key, value);
	        }
	        u.readMapEnd();
	        return map;
	}

	public static void main(String []args){
		TsetBean nei = new TsetBean();
		Map mapnei = new HashMap();
		mapnei.put("1n", "111n");
		mapnei.put("2n", "222n");
		mapnei.put("3n", "333n");
		nei.setAa("77");
		nei.setBb(88);
		nei.setMap(mapnei);
		
		
		
		TsetBean tsetBean = new TsetBean();
		Map map = new HashMap();
		map.put("1", "111");
		map.put("2", "222");
		map.put("3", "333");
		tsetBean.setAa("44");
		tsetBean.setBb(55);
		tsetBean.setMap(map);
		tsetBean.setTest(nei);
		MessagePack msgpack = new MessagePack();
		try {
			System.out.println(msgpack.read(msgpack.write(tsetBean, JavaClassTemplete.getInstance()), JavaClassTemplete.getInstance()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}