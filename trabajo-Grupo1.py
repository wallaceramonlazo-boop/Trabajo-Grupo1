from matplotlib import pyplot as plt
from abc import ABC, abstractmethod

class Divisivilidad(ABC): 
   def __str__(self, n):  
       self.__n = n
       self.__Divisor = CalcularDivisores()
       self.__A = []
      
   def CalcularDivisores(self):
      return [i for i in range(1, self.__n + 1) if self.__n % i == 0]
   def rel_divisibilidad(self):
     pares = CalcularDivisores()
     
   def MostrarConsola(self):
     for i in self.__A:
       print("kbro")
#Menu
def main ():
  print("Relacion de divisibilidad")
  try:
    Maximo = int(input("Ingrese un numero n: "))
    Control = Divisivilidad(Maximo)
    if n <= 0:
      print("Ingrese un numero mayor a cero ")
       
