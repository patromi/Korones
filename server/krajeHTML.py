# -*- coding: utf-8 -*-
from bs4 import BeautifulSoup
import requests
import googletrans
import csv
import difflib
import sys
import datetime
import time
from googletrans import Translator
def funkcja():
    dzialy = ['North America', 'Europe' , 'South America' , 'Asia' , 'Africa' , 'Oceania' , '' , '\n' , 'World','Total:']
    source = requests.get('https://www.worldometers.info/coronavirus/#c-all%22').text
    list = ['country', 'allcases','newcases', 'alldeads', 'newdeath', 'allrecived', 'activecases', 'critical','cases/1m','death/1m','alltest','test1/m','population', 'continent']
    soup = BeautifulSoup(source, 'lxml')
    pol = ['Australia', 'Austria', 'Azerbejdżan', 'Albania', 'Algieria', 'Angola', 'Andora', 'Antigua i Barbuda', 'Argentyna', 'Armenia', 'Afganistan', 'Arabia Saudyjska', 'Bahamy', 'Bangladesz', 'Barbados', 'Bahrajn', 'Belize', 'Białorusi', 'Belgia', 'Benin', 'Bułgaria', 'Boliwia', 'Bośnia i Hercegowina', 'Botswana', 'Brazylia', 'Brunei', 'Burkina Faso', 'Burundi', 'Bhutan', 'Cape Verde', 'Cypr', 'Chiny', 'Chorwacja', 'Czad', 'Czarnogóra', 'Chile', 'Dania', 'Demokratyczna Republika Konga', 'Dżibuti', 'Dominika', 'Egipt', 'El Salvador', 'Ekwador', 'Erytrea', 'Estonia', 'Etiopia', 'Fidżi', 'Filipiny', 'Finlandia', 'Francja', 'Gabon', 'Gujana', 'Gambia', 'Ghana', 'Gwatemala', 'Gwinea', 'Gwinea Bissau', 'Grenada', 'Grecja', 'Gruzja', 'Gwinea Równikowa', 'Haiti', 'Honduras', 'Hongkong', 'Hiszpania', 'Holandia', 'Izrael', 'Indie', 'Indonezja', 'Irak', 'Iran', 'Irlandia', 'Islandia', 'Jordan', 'Jemen', 'Jamajka', 'Japonia', 'Kazachstan', 'Kambodża', 'Kamerun', 'Kanada', 'Katar', 'Kenia', 'Kirgistan', 'Kiribati', 'KRLD', 'Kolumbia', 'Komory', 'Kosowo', 'Kostaryka', 'Kuba', 'Kuwejt', 'Laos', 'Lesotho', 'Liberia', 'Liban', 'Libia', 'Litwa', 'Liechtenstein', 'Luksemburg', 'Mauritius', 'Mauretania', 'Madagaskar', 'Malawi', 'Malezja', 'Mali', 'Malediwy', 'Malta', 'Maroko', 'Meksyk', 'Mozambik', 'Mołdawia', 'Monako', 'Mongolia', 'Myanmar', 'Macedonia', 'Mikronezja', 'Niemcy', 'Namibia', 'Nauru', 'Nepal', 'Niger', 'Nigeria', 'Nikaragua', 'Niue', 'Nowa Zelandia', 'Norwegia', 'Oman', 'Pakistan', 'Palau', 'Panama', 'Papua Nowa Gwinea', 'Paragwaj', 'Peru', 'Polska', 'Portugalia', 'Republika Dominikańska', 'Republika Konga', 'Republika Korei', 'Rosja', 'Rwanda', 'Rumunia', 'Republika Środkowoafrykańska', 'Republika Czeska', 'RPA', 'Samoa', 'San Marino', 'Suazi', 'Seszele', 'Senegal', 'Saint Vincent i Grenadyny', 'Saint Kitts i Nevis', 'Saint Lucia', 'Serbia', 'Singapur', 'Syria', 'Słowacja', 'Słowenia', 'Somalia', 'Sudan', 'Surinam', 'Sierra Leone', 'Szwajcaria', 'Szwecja', 'Sri lanka', 'Sudan Południowy', 'Timor Wschodni', 'Tadżykistan', 'Tajlandia', 'Tajwan', 'Tanzania', 'Togo', 'Tonga', 'Trynidad i Tobago', 'Tuvalu', 'Tunezja', 'Turkmenistan', 'Turcja', 'USA', 'Uganda', 'Uzbekistan', 'Ukraina', 'Urugwaj', 'Vanuatu', 'Watykan', 'Wielka Brytania', 'Węgry', 'Wenezuela', 'Wietnam', 'Włochy', 'Wybrzeże Kości Słoniowej', 'Wyspy Marshalla', 'Wyspy Cooka', 'Wyspy Świętego Tomasza i Książęca', 'Wyspy Salomona', 'Zambia', 'Zimbabwe', 'ZEA', 'Łotwa']
    got = ''
    golf = 0
    for article in soup.find_all('tbody'):
        for zmienna in article.find_all('tr'):
            golf += 1
            a = -1
            yu = 0
            for zmienna2 in zmienna.find_all('td'):
                if a == -1:
                    a +=1
                    continue
                if a == 14:
                    continue
                if a == 0:
                    kraj = zmienna2
                    kraj = kraj.text.replace('</nobr>','').replace('\n','')
                    if kraj in dzialy:
                        break

                    got += kraj
                    got += '\n'
                    a +=1





    got += '\n'
    return got
print(funkcja())