package util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import drugRelated.Item;
import recRelated.Rating;

public class Utils {

	public static boolean doesContain(Set<Item> allItems,
			String name) {
		boolean retVal = false;
		for(Item item: allItems){
			boolean doesContain = item.doesContain(name);
			if(doesContain == true){
				retVal = true;
				break;
			}
		}
		return retVal;
	}

	public static Item getItem(Set<Item> allItems, String name) {
		Item retVal = null;
		for(Item item: allItems){
			boolean doesContain = item.doesContain(name);
			if(doesContain == true){
				retVal = item;
				break;
			}
		}
		return retVal;
	}

	
}
