import networkx as nx
from matplotlib import pyplot as plt
from abc import ABC, abstractmethod
from colorama import Fore


class Divisibilidad(ABC):
    def __init__(self, n):
        self._n = n
        self._Divisores = self.CalcularDivisores()

    def CalcularDivisores(self):
        return [i for i in range(1, self._n + 1) if self._n % i == 0]

    def rel_divisibilidad(self):
        pares = []
        for a in self._Divisores:
            for b in self._Divisores:
                if b % a == 0 and a != b: 
                    pares.append((a, b))
        return pares

    def aristas_hasse(self):
        pares = self.rel_divisibilidad()
        aristas_directas = []
        for a, b in pares:
            es_directo = True
            for c in self._Divisores:
                if c != a and c != b:
                    if c % a == 0 and b % c == 0:
                        es_directo = False 
                        break
            if es_directo:
                aristas_directas.append((a, b))
        return aristas_directas

    def MostrarConsola(self):
        print(f"Divisores de {self._n}:")
        for i in self._Divisores:
            print(i)
   
    @abstractmethod
    def mostrar(self):
        pass


class DivisibilidadGrafica(Divisibilidad):
    def mostrar(self):

        G= nx.DiGraph()
        
        G.add_nodes_from(self._Divisores)
        G.add_edges_from(self.aristas_hasse())

        niveles = {}
        for nodo in G.nodes():
            copia = nodo
            nivel = 0
            d = 2
            while d * d <= copia:
                while (copia % d) == 0:
                    nivel += 1
                    copia //= d
                d += 1
            if copia > 1:
                nivel += 1
            
            if nivel not in niveles:
                niveles[nivel] = []
            niveles[nivel].append(nodo)

        posicion = {}
        for niv, nodos in niveles.items():
            ancho = len(nodos)
            for i, nodo in enumerate(nodos):
                x = (i - (ancho - 1) / 2) 
                y = niv                   
                posicion[nodo] = (x, y)

        plt.figure(figsize=(8, 8))
        
        nx.draw(G, posicion, with_labels=True, 
                node_color='white',      
                font_size=28,             
                font_color='black', 
                node_size=1500, 
                edge_color='darkblue',    
                width=2.5,                
                arrows=False)             
        
        plt.show()


def main():
    print("Relacion de divisibilidad")
    try:
        Maximo = int(input("Ingrese un numero n: "))
        if Maximo <= 0:
            print(f"{Fore.YELLOW}Ingrese un numero mayor a cero {Fore.WHITE}")
        else:
            print("")
            print(f"{Fore.GREEN}Numero ingresado correctamente {Fore.WHITE}")
            Control = DivisibilidadGrafica(Maximo)
            print("")    
            op= 0

            while op!=4:
                print(f"1.Mostrar Divisores del numero '{Maximo}'")
                print(f"2.Mostrar Diagrama de Hasse del numero '{Maximo}'")
                print(f"3.Mostrar Relacion de divisibilidad del numero '{Maximo}'")
                print(f"4.Salir ")
                try: 
                    op= int(input(f"{Fore.LIGHTBLUE_EX}Elija una opcion: {Fore.WHITE}"))
                    match op:
                        case 1: 
                            print(Control._Divisores)
                            print("")
                        case 2:
                            Control.mostrar()
                            print("")
                        case 3:
                            print(Control.rel_divisibilidad())
                            print("")
                        case 4:
                            print("Saliendo ")
                        case _:
                            print(f"{Fore.RED}Opcion no valida {Fore.WHITE}")     
                            print("") 
                except ValueError:
                    print (f"{Fore.RED}Ingrese un valor valido {Fore.WHITE}")
                    print("")
    except ValueError:
        print(f"{Fore.RED}Ingrese un valor valido {Fore.WHITE}")


main()
