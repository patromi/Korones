# -*- coding: utf-8 -*-
from bs4 import BeautifulSoup
import requests
import csv
import sys
import datetime
import time
import urllib
def funkcja(zmienna):
    source = requests.get('https://www.google.pl/search?client=firefox-b-d&q=Sanepid+psse+kontakt+telefon' + zmienna + 'sanepid' + '&lr=lang_pl').text
    soup = BeautifulSoup(source, 'lxml')

    try:
        match = soup.find_all('span',{'class': 'BNeawe tAd8D AP7Wnd'})
        #match = match[0].text
        match = match[len(match) -1]
        match = match.text
        try:
            x = match
            x = x.replace(' ','')
            x = int(x)
            match = str(match)
            xw = soup.find_all('div', {'class': 'BNeawe tAd8D AP7Wnd'})
            xw = xw[0]
            xw = xw.text
            xw = xw.split('\n')
            xw = xw[1]
            wh = soup.find_all('span', {'class': 'BNeawe tAd8D AP7Wnd'})
            wh = wh[0]
            wh = wh.text
            wx = '/nazwa;' + xw
            wx += '/ulica;' + wh
            wx += '/numer;' + match +'\n'
            return wx

        except:

            match = '/nazwa;null' + '\n'
            return match
    except:
        match = '/nazwa;null\n'
        return match








