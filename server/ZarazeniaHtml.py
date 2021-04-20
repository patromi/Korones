# -*- coding: utf-8 -*-
from bs4 import BeautifulSoup
import requests
import csv
import sys
import datetime
import time
def funkcja():
    lista_kraje= []
    source = requests.get('https://www.worldometers.info/coronavirus/').text
    soup = BeautifulSoup(source, 'lxml')
    match = soup.find('div', class_='maincounter-number')
    match = match.text
    match = match.replace('\n', '/zarazenia;', 1)





    return match
print(funkcja())
