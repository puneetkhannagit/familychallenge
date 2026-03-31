package com.example.microservices.bold;

import java.util.*;

public class Test {

    public static void main(String[] args) {
        Employee employee1= new Employee(1,"puneet",1000,"abc");
        Employee employee2= new Employee(1,"krishna",130000,"abc");

        Employee employee3= new Employee(1,"varsha",89898,"xyz");
        Employee employee4= new Employee(1,"a",56565667,"abc");
        Employee employee10= new Employee(1,"b",8787,"abc");
        Employee employee11= new Employee(1,"c",787,"abc");
        Employee employee12= new Employee(1,"akansh",78788,"abc");
        Employee employee5= new Employee(1,"Shivam",5767,"abcd");
        List<Employee> all= new ArrayList<>();
        all.add(employee1);
        all.add(employee2);
        all.add(employee3);
        all.add(employee4);
        all.add(employee5);
        all.add(employee10);
        all.add(employee11);
        all.add(employee12);

        all.add(employee5);

        Map<String, List<Employee>> depAndEmployees= new HashMap<>();

        for(Employee emp: all){
            String dep= emp.getDepartmentId();
            List<Employee> emps= depAndEmployees.getOrDefault(dep,new ArrayList<>());
            emps.add(emp);
            depAndEmployees.put(dep,emps);
        }

        Map<String,List<Employee>> ansMap= new HashMap<>();

        for(String dept: depAndEmployees.keySet()){

            List<Employee> emps= depAndEmployees.get(dept);
            Collections.sort(emps,(a,b)->(b.getSalary()-a.getSalary()));;
            if(emps.size()<1) continue;;
            List<Employee> sortedEmployees= new ArrayList<>();

            if(sortedEmployees.size()<=3){
                ansMap.put(dept,emps);
                continue;
            }else{
                for(int i=0;i<3;i++){
                    sortedEmployees.add(emps.get(i));
                }
                ansMap.put(dept,sortedEmployees);
            }


        }

        System.out.println(ansMap);







    }
}
