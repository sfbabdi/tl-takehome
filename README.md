xformLog.py - Transform log from Google Spreadsheet

Data export are found in `data/` folder.

Sample output:
```
# Transform TL log
(v-env) [tl-takehome]$ ./xformLog.py tl
(v-env) [tl-takehome]$ head tl.csv
tl_os_id,tl_browser_id,tl_domain_id,os_name,browser_name,domain_name,count
0,0,35756,Unknown,Unknown,laptopmag.com,19
0,0,36687,Unknown,Unknown,androidcentral.com,118
0,0,37772,Unknown,Unknown,imore.com,48
0,0,37908,Unknown,Unknown,tomshardware.com,42
0,0,38055,Unknown,Unknown,howtogeek.com,56
0,0,39442,Unknown,Unknown,toptenreviews.com,19
0,0,39751,Unknown,Unknown,livescience.com,46
0,0,49672,Unknown,Unknown,space.com,20
0,0,50742,Unknown,Unknown,tomsguide.com,44

# Transform Client log
(v-env) [tl-takehome]$ ./xformLog.py 3party
(v-env) [tl-takehome]$ head client.csv
tl_os_id,tl_browser_id,tl_domain_id,os_name,browser_name,domain_name,count
0,0,35756,Unknown,Unknown,laptopmag.com,19
0,0,36687,Unknown,Unknown,androidcentral.com,118
0,0,37772,Unknown,Unknown,imore.com,48
0,0,37908,Unknown,Unknown,tomshardware.com,42
0,0,38055,Unknown,Unknown,howtogeek.com,56
0,0,39442,Unknown,Unknown,toptenreviews.com,19
0,0,39751,Unknown,Unknown,livescience.com,46
0,0,49672,Unknown,Unknown,space.com,20
0,0,50742,Unknown,Unknown,tomsguide.com,44
```

