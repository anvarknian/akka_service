*****Делаем HTTP POST таким образом :*****

    curl -H "Content-type: application/json" -X POST -d '{"key": "31", "value": 31}' http://0.0.0.0:8080/numbers

   _где key и value могут быть любые Int._
   
   
****Здесь можно найти сумму чисел которых мы получили по POST****

        http://0.0.0.0:8080/numbers
        
        
****Также можем получить любую цифру в виде {"key": "31", "value": 31}**** 

        http://0.0.0.0:8080/numbers/31
        
        
**Для завершения сервиса нажимаем на Enter****