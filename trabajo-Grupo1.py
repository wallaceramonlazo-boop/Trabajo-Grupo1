from matplotlib import pyplot as plt
from abc import ABC, abstractmethod


class Divisibilidad(ABC):
    def __init__(self, n):
        self.__n = n
        self.__Divisores = self.CalcularDivisores()

    def CalcularDivisores(self):
        return [i for i in range(1, self.__n + 1) if self.__n % i == 0]

    def rel_divisibilidad(self):
        return self.CalcularDivisores()

    def MostrarConsola(self):
        print(f"Divisores de {self.__n}:")
        for i in self.__Divisores:
            print(i)
   
    @abstractmethod
    def mostrar(self):
        pass


class DivisibilidadConcreta(Divisibilidad):
    def mostrar(self):
        self.MostrarConsola()


def main():
    print("Relacion de divisibilidad")
    try:
        Maximo = int(input("Ingrese un numero n: "))
        if Maximo <= 0:
            print("Ingrese un numero mayor a cero")
        else:
            Control = DivisibilidadConcreta(Maximo)
            Control.mostrar()
    except ValueError:
        print("Ingrese un numero valido")


main()
       
