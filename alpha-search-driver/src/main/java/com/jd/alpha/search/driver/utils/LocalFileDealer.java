package com.jd.alpha.search.driver.utils;

import com.jd.alpha.search.driver.model.Neta;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangshangzhi on 2018/3/21.
 */
public class LocalFileDealer {

    private final static String file = "neta.txt";

    public static List<Neta> readNetaData() {
        List<Neta> list = new ArrayList<>();

        FileInputStream fis;
        InputStreamReader isr;
        BufferedReader br;
        String line = null;
        Map<String, Integer> provenanceCountMap = new HashMap<>();
        int i = 0;
        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                String[] array = line.split("\t");
                String provenance = array[0].trim();
                String charactor = array[1].trim();
                String sentence = array[2].trim();
                double popular = 0;
                if (array.length >= 4) {
                    popular = Double.parseDouble(array[3].trim());
                }

                if (!provenanceCountMap.containsKey(provenance)) {
                    provenanceCountMap.put(provenance, 1);
                } else {
                    provenanceCountMap.put(provenance, provenanceCountMap.get(provenance) + 1);
                }

                Neta neta = new Neta();
                neta.setProvenance(provenance);
                neta.setCharactor(charactor);
                neta.setTopicId(provenanceCountMap.get(provenance));
                neta.setPopular(popular);

                String[] contents = sentence.split("\\.");
                int innerIndex = 0;
                for (String ele : contents) {
                    i++;
                    innerIndex++;
                    Neta netaC = neta.clone();
                    netaC.setId(i);
                    netaC.setCellIdInTopic(innerIndex);
                    netaC.setContent(ele);
                    list.add(netaC);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
