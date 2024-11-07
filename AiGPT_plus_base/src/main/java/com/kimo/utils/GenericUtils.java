package com.kimo.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class GenericUtils {

    /**
     * 获取热项的基类方法
     *
     * @param maxInsert 最大插入到 Redis 的数量
     * @param fetcher        数据获取函数，根据页码获取数据列表
     * @param <T>           数据类型
     * @return 返回所有获取到的对象列表
     */
    public static <T> List<T> getItemsBase(int maxInsert, Function<Integer, List<T>> fetcher) {
        // 假设我们传入0来获取总数
        List<T> firstPage = fetcher.apply(0);
        int itemCount = firstPage.size();

        if (itemCount == 0) {
            return Collections.emptyList(); // 返回空列表而不是 false
        }

        int step = maxInsert;
        int total = (step % itemCount == 0) ? step / itemCount : step / itemCount + 1;
        List<T> allItems = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            List<T> itemsByPage = fetcher.apply(i * step);
            allItems.addAll(itemsByPage);
        }

        return allItems; // 返回获取到的所有对象
    }


}
