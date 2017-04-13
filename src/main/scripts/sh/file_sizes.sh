#!/bin/bash

while true; do echo 'select tot.c as processed, tot.a / 1024 / 1024 as avg_MB, tot.s / 1024 / 1024 / 1024 as tot_GB, tot.m / 1024 / 1024 as biggest_MB from (select count(identifier) as c, sum(total_file_size) as s, avg(total_file_size) as a, max(total_file_size) as m  from oai_records where process_status_code = 100) as tot;' | mysql -udaredev -pdaredev dare --table 2> /dev/null; sleep 1; clear; done
