package dev.ftb.mods.ftblibrary.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MapUtils {

	public static <K, V> Map<K, V> sortMapByKey(Map<K, V> map, Comparator<K> comparator) {
		return map.entrySet().stream()
				.sorted(Map.Entry.comparingByKey(comparator))
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(a, b) -> a,
						HashMap::new
				));
	}

	public static <K extends Comparable<? super K>, V> Map<K, V> sortMapByKey(Map<K, V> map) {
		return map.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(a, b) -> a,
						HashMap::new
				));
	}
}