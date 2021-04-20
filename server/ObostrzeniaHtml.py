# -*- coding: utf-8 -*-
from bs4 import BeautifulSoup
import requests
import csv
import sys
import datetime
import time
def funkcja():
    lista_kraje= []
    source = requests.get('https://www.gov.pl/web/koronawirus/3etap').text
    soup = BeautifulSoup(source, 'lxml')

    match = soup.find('div', class_='editor-content')
    match = str(match)
    match = match.replace('\n','\n/obo;')
    match += '\n'
    match += '/obo;<div><p><a href="https://www.gov.pl/web/koronawirus/3etap">Zródło: https://www.gov.pl/web/koronawirus/3etap</a></p></div>\n'






    return match



