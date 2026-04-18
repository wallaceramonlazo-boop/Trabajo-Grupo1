from matplotlib import pyplot as plt


class RelacionDivisibilidad:

    def __init__(self, n):
        self.__n = n
        self.__D = self.__calcular_divisores()
        self.__A = []

    def __calcular_divisores(self):
        return [i for i in range(1, self.__n + 1) if self.__n % i == 0]

    def __es_cobertura(self, a, b):
        return not any(
            c != a and c != b and c % a == 0 and b % c == 0 for c in self.__A
        )

    def __pares(self):
        return [(a, b) for a in self.__A for b in self.__A if a != b and b % a == 0]

    def __cobertura(self):
        return [(a, b) for a, b in self.__pares() if self.__es_cobertura(a, b)]

    def __niveles(self):
        niv = {a: 0 for a in self.__A}
        cambio = True
        while cambio:
            cambio = False
            for a, b in self.__cobertura():
                if niv[b] <= niv[a]:
                    niv[b] = niv[a] + 1
                    cambio = True
        return niv

    def __maximales(self):
        return [a for a in self.__A if not any(b != a and b % a == 0 for b in self.__A)]

    def __minimales(self):
        return [a for a in self.__A if not any(b != a and a % b == 0 for b in self.__A)]

    def __posiciones(self):
        niv = self.__niveles()
        pos = {}
        grupos = {}
        for a in self.__A:
            grupos.setdefault(niv[a], []).append(a)
        for nivel, nodos in grupos.items():
            nodos.sort()
            total = len(nodos)
            for i, nodo in enumerate(nodos):
                x = (i - (total - 1) / 2) * 2
                pos[nodo] = (x, nivel)
        return pos

    def seleccionar_subconjunto(self, opcion):
        if opcion == 1:
            self.__A = self.__D.copy()
        else:
            self.__A = []
            print("Ingrese elementos (-1 para terminar):")
            while True:
                x = int(input("  > "))
                if x == -1:
                    break
                if x in self.__D:
                    self.__A.append(x)
                else:
                    print(f"  {x} no esta en D")

    def mostrar_consola(self):
        niv = self.__niveles()
        print(f"\nD({self.__n}) = {self.__D}")
        print(f"A            = {self.__A}")
        print(f"Pares (a|b)  = {self.__pares()}")
        print(f"Maximales    : {self.__maximales()}")
        print(f"Minimales    : {self.__minimales()}")
        print("\n--- DIAGRAMA DE HASSE (texto) ---")
        if niv:
            for n in range(max(niv.values()), -1, -1):
                nodos = [a for a in self.__A if niv[a] == n]
                print(f"  Niv {n}: {nodos}")
                if n > 0:
                    aristas = [(a, b) for a, b in self.__cobertura() if niv[b] == n]
                    print(f"  aristas: {[f'{a}->{b}' for a, b in aristas]}")

    def graficar_hasse(self):
        pos = self.__posiciones()
        cob = self.__cobertura()

        fig, ax = plt.subplots(figsize=(8, 6))
        ax.set_facecolor("#f9f9f9")
        fig.patch.set_facecolor("#f9f9f9")
        ax.set_title(
            f"Diagrama de Hasse — Divisibilidad en D({self.__n})",
            fontsize=13, fontweight="bold", pad=15,
        )
        ax.axis("off")

        for a, b in cob:
            x1, y1 = pos[a]
            x2, y2 = pos[b]
            ax.annotate("", xy=(x2, y2), xytext=(x1, y1),
                        arrowprops=dict(arrowstyle="-", color="#555555", lw=1.5))

        for nodo, (x, y) in pos.items():
            if nodo in self.__maximales():
                color = "#4CAF50"
            elif nodo in self.__minimales():
                color = "#2196F3"
            else:
                color = "#FF9800"

            circulo = plt.Circle((x, y), 0.35, color=color, zorder=3)
            ax.add_patch(circulo)
            ax.text(x, y, str(nodo), ha="center", va="center",
                    fontsize=11, fontweight="bold", color="white", zorder=4)

        # Leyenda sin mpatches
        leyenda = [
            plt.Line2D([0],[0], marker='o', color='w', markerfacecolor="#2196F3", markersize=12, label="Minimal"),
            plt.Line2D([0],[0], marker='o', color='w', markerfacecolor="#FF9800", markersize=12, label="Intermedio"),
            plt.Line2D([0],[0], marker='o', color='w', markerfacecolor="#4CAF50", markersize=12, label="Maximal"),
        ]
        ax.legend(handles=leyenda, loc="lower right", fontsize=9)

        xs = [x for x, y in pos.values()]
        ys = [y for x, y in pos.values()]
        ax.set_xlim(min(xs) - 1, max(xs) + 1)
        ax.set_ylim(min(ys) - 1, max(ys) + 1)

        plt.tight_layout()
        plt.show()


# ── MAIN ──
n = int(input("Ingrese n: "))
r = RelacionDivisibilidad(n)
op = int(input("Subconjunto A — [1] Todo D  [2] Manual: "))
r.seleccionar_subconjunto(op)
r.mostrar_consola()
r.graficar_hasse()