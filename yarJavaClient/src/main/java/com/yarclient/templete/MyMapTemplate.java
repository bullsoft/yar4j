package com.yarclient.templete;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.MapTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;


public class MyMapTemplate<K, V> extends MapTemplate<K, V>{

	private Template<K> keyTemplate;
    private Template<V> valueTemplate;
    
	public MyMapTemplate(Template<K> keyTemplate, Template<V> valueTemplate) {
		super(keyTemplate, valueTemplate);
		this.keyTemplate = keyTemplate;
		this.valueTemplate = valueTemplate;
	}
	
	public void write(Packer pk, Map<K, V> target, boolean required)
            throws IOException {
        if (!(target instanceof Map)) {
            if (target == null) {
                if (required) {
                    throw new MessageTypeException("Attempted to write null");
                }
                pk.writeNil();
                return;
            }
            throw new MessageTypeException("Target is not a Map but " + target.getClass());
        }
        Map<K, V> map = (Map<K, V>) target;
        pk.writeMapBegin(map.size());
        for (Map.Entry<K, V> pair : map.entrySet()) {
            keyTemplate.write(pk, pair.getKey());
            valueTemplate.write(pk, pair.getValue());
        }
        pk.writeMapEnd();
    }
	
	public Map<K, V> read(Unpacker u, Map<K, V> to, boolean required)
            throws IOException {
        if (!required && u.trySkipNil()) {
            return null;
        }
        int n = u.readMapBegin();
        Map<K, V> map;
        if (to != null) {
            map = (Map<K, V>) to;
            map.clear();
        } else {
            map = new HashMap<K, V>(n);
        }
        for (int i = 0; i < n; i++) {
            K key = keyTemplate.read(u, null);
            if(key == null){
            	continue;
            }else if(key instanceof String){
            	key = (K) ((String) key).replaceAll("\\u0000.+\\u0000", "");
            }
            V value = valueTemplate.read(u, null);
            map.put(key, value);
        }
        u.readMapEnd();
        return map;
    }

	

}