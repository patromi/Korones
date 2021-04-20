# -*- coding: utf-8 -*-
import socket, threading
import Html
import sys
import ObostrzeniaHtml
import ObjawyPrawdziwe
import time
import ZarazeniaHtml
import krajeHTML
import NumerMazowsze
import pingpong
list = []

class Klient(threading.Thread):
    def __init__(self,clientAddress,clientsocket):
        threading.Thread.__init__(self)
        self.csocket = clientsocket
        list.append(clientsocket)
        print ("Nowe polaczenie od", clientAddress)
    def run(self):
        print ("Polaczono z: ", clientAddress)
        self.csocket.send(bytes("Polaczono ze serwerem:" + '\n'))
        msg = ''
        #################
        #Sekcja  klienta#
        #################
        while True:
            data = self.csocket.recv(2048)
            msg = data.decode('UTF-8')
            if len(data) == 0:
                break
            #Komenda /informacje; Wysłanie do użytowników
            if '/info;' in msg:
                warszawa = ''
                poznan = 0
                jawa = Html.funkcja().split('[]')
                msg = msg.replace('\r\n', '')
                for d in jawa:
                    if poznan == 13 :
                        break
                    else:
                        if msg in d:
                            warszawa += d
                            warszawa += '\n'
                            poznan += 1
                self.csocket.send(warszawa.encode('UTF-8'))
                warszawa = ''
            #Komenda obostrzenia wyłyła raport o aktualnych obostrzeniach
            if '/obostrzenia' in msg:
                obostrzenia = ObostrzeniaHtml.funkcja()
                self.csocket.send(obostrzenia.encode('UTF-8'))
            #Komenda numer wysyła informacje o Sanepidzie
            if '/numer;' in msg:
                dachowa = msg.split(';')
                bariera = dachowa[1]
                print(bariera)
                satelita = NumerMazowsze.funkcja(bariera)
                satelita = str(satelita)
                self.csocket.send(satelita.encode('UTF-8'))
            #Komenda tabela wysyła ile jest krajów na świecie
            if '/tabela' in msg:
                tabela = '/tabela;215\n'
                self.csocket.send(tabela.encode('UTF-8'))
            #Komenda Objawy wysyła raport o aktualnych objawach
            if '/objawy' in msg:
                objawy = ObjawyPrawdziwe.funkcja()
                self.csocket.send(objawy.encode('UTF-8'))
            #Komenda /ping sprawdza połączenie z aplikacją
            if '/ping;' in msg:
                ping = msg.split(';')
                pong = '/pong;' + ping[1] + '\n'
                self.csocket.send(pong.encode('UTF-8'))
            #Komenda /zarażenia sprawdza aktualną ilość zachorowań
            if '/zarazenia' in msg:
                zarazenia = ZarazeniaHtml.funkcja()
                zarazenia = str(zarazenia)
                self.csocket.send(zarazenia.encode('UTF-8'))
            #Komenda /kraj wysyła wszystkie nazwy krajów po polsku jak i po angielsku
            if '/kraj' in msg:
                kraj = pingpong.funkcja()
                kraj = str(kraj)
                self.csocket.send(kraj.encode('UTF-8'))
            #Komenda umożliwiająca wysyłanie powiadomień do innych użytkowników za pomocą naszej aplikacji testowej
            if '/powiadomienie;' in msg:
                winogrady = msg.split(';')
                winogrady = winogrady[1]
                piatkowo = '/powiadomienie;'
                piatkowo += winogrady
                piatkowo += '\n'
                for wiad in list:
                    wiad.send(bytes(piatkowo.encode("UTF-8")))
            #Wyłącza serwer
            if '/koniec' in msg:
                    sys.exit()

            print ("from client",'',clientAddress,'', msg)
        print ("Client at ", clientAddress , " disconnected...")
        list.remove(self.csocket)
class Watek(threading.Thread):
    def run(self):
        ##################
        ##Sekcja Serwera##
        ##################
        #Komendy serwerowe
        while True:
            wiadomosc = raw_input()
            for i in list:
                if list == []: #Wysyłanie wiadomości
                    print("Nie ma do kogo wysłać")
                else:
                    print('Wysylanie:', i)
                    i.send(bytes(wiadomosc + '\n'))
            if wiadomosc == '/stop':
                sys.exit()
            if wiadomosc == '/inwsz':
                print('Wysylanie raportu do wszystkich uzytkownikow')
                wiadomosc = Html.funkcja()
                for i in list:
                    i.send(wiadomosc.encode('UTF-8'))
            if wiadomosc == '/lista':
                    print(list)
################
#config Serwera#
################
LOCALHOST = ''
PORT = 20001
server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server.bind((LOCALHOST, PORT))
print("Serwer staruje")
print("Czekanie na klienta")
Watek().start()
while True:
    server.listen(1)
    clientsock, clientAddress = server.accept()
    newthread = Klient(clientAddress, clientsock)
    newthread.start()
