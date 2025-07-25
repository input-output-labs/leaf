package fr.iolabs.leaf.common;

import java.util.Map;

public interface ILeafModular {
	public String getId();
	public Map<String, Object> getModules();
	public void setModules(Map<String, Object> modules);
	public Map<String, String> getGenericData();
	public void setGenericData(Map<String, String> genericData);
}
