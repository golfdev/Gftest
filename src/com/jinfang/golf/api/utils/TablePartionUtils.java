package com.jinfang.golf.api.utils;

public class TablePartionUtils {

	public static void main(String[] args) {
		for(int i=0;i<100;i++){
			System.out.println("CREATE TABLE `follow_relation_"+i+"` (`host` int(11) DEFAULT NULL,`guest` int(11) DEFAULT NULL,`status` tinyint(4) DEFAULT '0',`created_time` timestamp NULL DEFAULT NULL,UNIQUE KEY `host` (`host`,`guest`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		}

	}

}
