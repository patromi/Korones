# -*- coding: utf-8 -*-
from bs4 import BeautifulSoup
import requests
import csv
import sys
import datetime
import time
def funkcja():
    dzialy = ['North America', 'Europe' , 'South America' , 'Asia' , 'Africa' , 'Oceania' , '' , '\n' , 'World','Total:']
    source = requests.get('https://www.worldometers.info/coronavirus/#c-all%22').text
    list = ['country', 'allcases','newcases', 'alldeads', 'newdeath', 'allrecived','activecases','activecases', 'critical','cases/1m','death/1m','alltest','test1/m','population', 'continent','e']
    soup = BeautifulSoup(source, 'lxml')
    csv_file = open('cms_scrape.csv', 'w')
    got = ''
    golf = 0
    bawara = ''
    csv_writer = csv.writer(csv_file)
    csv_writer.writerow(['headline', 'summary', 'video_link'])
    czas = time.time() * 1000
    czas = round(czas, 0)
    czas = int(czas)
    czas = str(czas)
    for article in soup.find_all('tbody'):
        for zmienna in article.find_all('tr'):
            golf += 1
            a = -1
            yu = 0
            for zmienna2 in zmienna.find_all('td'):
                krzeslo = 0
                if a == -1:
                    a +=1
                    continue
                if a == 15:
                    continue
                if a == 0:
                    kraj = zmienna2

                    kraj = kraj.text.replace('</nobr>','').replace('\n','')
                    a +=1
                else:
                    b = len(zmienna.find_all('td'))
                    if not kraj in dzialy:
                        if a == 6:
                            a += 1
                            yu += 1
                            continue
                            grabowska = kraj + ';' + czas + ';' + list[6] + ';' + zmienna2.text.replace('\n', '').replace(',', '').replace('+', '')
                            got += '[][][]]/info;'
                            got += grabowska + '\n'
                        else:

                            bawara = kraj + ';' + czas  + ';' + list[a] + ';' + zmienna2.text.replace('\n', '').replace(',', '').replace('+', '')
                            bawara = bawara.split(';')
                            if bawara[3] =='':
                                bawara[3] = 'null'
                            bawara = ';'.join(bawara)

                            got += '[]/info;'
                            bawara = bawara.replace('N/A', 'null')

                            got += bawara + '\n'

                    else:
                        continue

                    a += 1
                    yu +=1

    csv_file.close()
    return got
print(funkcja())
