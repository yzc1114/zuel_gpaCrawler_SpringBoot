package com.yzchnb.gpacrawler.demo.CrawlService.Crawler.Entities;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.round;

public class BeReturnedInfo {
    public HashMap<String, Float> all_info = new HashMap<>();
    public ArrayList<TermInfo> each_term;

    public String parseDoc(Document doc) throws Exception{
        Elements trs = doc.select("tr");
        HashMap<String, ArrayList<LessonInfo>> termDict = new HashMap<>();
        for(int i = 2; i < trs.size(); i++){
            Elements all_td = trs.get(i).select("td");
            if(all_td.size() <= 10)
                break;
            LessonInfo lessonInfo = new LessonInfo();
            lessonInfo.lesson_name = all_td.get(4).text();
            lessonInfo.grade = Float.parseFloat(all_td.get(7).text());
            lessonInfo.credit = Float.parseFloat(all_td.get(8).text());
            lessonInfo.gpa = Float.parseFloat(all_td.get(10).text());
            String term_title = all_td.get(1).text();
            if(termDict.containsKey(term_title)){
                termDict.get(term_title).add(lessonInfo);
            }else{
                ArrayList<LessonInfo> a = new ArrayList<>();
                a.add(lessonInfo);
                termDict.put(term_title, a);
            }
        }
        each_term = new ArrayList<>();

        for (Map.Entry<String, ArrayList<LessonInfo>> entry : termDict.entrySet()) {
            String term = entry.getKey();
            ArrayList<LessonInfo> lessonInfoArrayList = entry.getValue();
            TermInfo termInfo = new TermInfo();
            termInfo.term = term;
            termInfo.grade = lessonInfoArrayList;
            each_term.add(termInfo);
        }

        each_term.sort(Comparator.comparing(o -> o.term));

        for (TermInfo termInfo : each_term) {
            float term_all_credits = 0;
            float term_weighting_credits = 0;
            float term_weighting_grades = 0;
            for (LessonInfo lessonInfo : termInfo.grade) {
                if (lessonInfo.grade >= 60.0) {
                    term_all_credits += lessonInfo.credit;
                    term_weighting_credits += lessonInfo.credit * lessonInfo.gpa;
                    term_weighting_grades += lessonInfo.credit * lessonInfo.grade;
                }
            }
            float term_average_gpa = term_weighting_credits / term_all_credits;
            float term_average_grades = term_weighting_grades / term_all_credits;
            termInfo.term_all_credits = (float) round(term_all_credits * 100)/100;
            termInfo.term_average_gpa = (float) round(term_average_gpa * 100)/100;
            termInfo.term_average_grades = (float) round(term_average_grades * 100)/100;
        }

        HashMap<String, LessonInfo> lesson_to_credits_grades_gpa = new HashMap<>();
        for(TermInfo termInfo : each_term){
            for(LessonInfo lessonInfo : termInfo.grade){
                boolean update = false;
                if(!lesson_to_credits_grades_gpa.containsKey(lessonInfo.lesson_name)){
                    update = true;
                }else{
                    if(lesson_to_credits_grades_gpa.get(lessonInfo.lesson_name).grade < lessonInfo.grade)
                        update = true;
                }
                if(update){
                    lesson_to_credits_grades_gpa.put(lessonInfo.lesson_name, lessonInfo);
                }
            }
        }

        float all_credits = 0;
        float all_weighting_grades = 0;
        float all_weighting_credits = 0;
        for(Map.Entry<String, LessonInfo> entry : lesson_to_credits_grades_gpa.entrySet()){
            LessonInfo lessonInfo = entry.getValue();
            if(lessonInfo.grade >= 60){
                all_credits += lessonInfo.credit;
                all_weighting_grades += lessonInfo.credit * lessonInfo.grade;
                all_weighting_credits += lessonInfo.credit * lessonInfo.gpa;
            }
        }
        float all_average_grades = all_weighting_grades / all_credits;
        float all_average_gpa = all_weighting_credits / all_credits;

        all_info.put("all_credits", (float) round(all_credits*100)/100);
        all_info.put("all_average_grades", (float) round(all_average_grades*100)/100);
        all_info.put("all_average_gpa", (float) round(all_average_gpa*100)/100);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
