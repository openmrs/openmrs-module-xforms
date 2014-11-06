package org.openmrs.module.xforms.util;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.context.Context;


public class LocationUtil {
	
	public Location get(String locationId) {
		if (StringUtils.isBlank(locationId)) {
			return null;
		}
		
		return Context.getLocationService().getLocation(Integer.parseInt(locationId));
	}
	
	public String getName(String locationId) {
		Location location = get(locationId);
		
		if (location != null) {
			return location.getName();
		}
		
		return null;
	}
}
