def calc_divisores(n):
  divisores= []
  for i in range (1, n+1):
    if n%1 == 0:
      divisores.append(i)
  return divisores 

def rel_divisibilidad(c):
  pares= []
  for i in c:
    for e in c:
      if b % a == 0:
        pares.append((a,b))
  return pares

def main ():
  print("Relacion de divisibilidad")
  try:
    n = int(input("Ingrese un numero n: "))
    if n <= 0:
      print("Ingrese un numero mayor a cero ")
      
    d= calc_divisores(n)
    print(f"Divisores de {n} {d}")
