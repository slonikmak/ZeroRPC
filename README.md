# RPC Сервис на jeroMQ (JSON версия)
Библиотека позволяет удалённо (по сети или из других процессов) вызывать методы объекта.

## Основные идеи  
- Возможность предоставить удалённый доступ к любому объекту
- Простое создание клиента/сервера без дополнительной генерации кода
- Использование клиентом интерфейса сервиса
- Передача аргументов и возвращаемых значений любого типа  
## Ограничения
- Интерфейс сервиса необходимо иметь в classpath клиента(посредством общего модуля), хотя можно создать и свой интерфейс, реализующий функциональность сервиса
- Передаваемые объекты так же должны находиться в classpath клиента и сервера в оинаковых пакетах
- Передаваемые и возвращаемые объекты не могут быть иммутабельными(надо исправить)

## Использование 
Сервис - класс, реализующий интерфейс, предоставляющий некоторую функциональность.  
Класс ZeroRPCService и ZeroRPCClient типизируются интерфейсом.
### Интерфейс сервиса
```java
interface MyService{
    String getStr();
}
```  
### Сам сервис
```java
class MyServiceImpl implements MyService{
    @Override
    public String getStr(){
        return "Answer";
    }
}
```
### Создание RPC сервиса
```java
class Main{
    public static void main(String[] args){
      //Создание сервиса
      MyService myService = new MyServiceImpl();
      //Оборачивание сервиса в RPC
      ZeroRPCService<MyService> zeroRPCService = new ZeroRPCService<>(myService, "tcp://*:5000");
      //Запуск сервиса
      zeroRPCService.start();
      //Остановка сервиса
      zeroRPCService.shutdown();
    }
}
```

### Создание клиента
```java
class Main{
    public static void main(String[] args){
        //Создание RPC клиента
        ZeroRPCClient<MyService> zeroRPCClient = new ZeroRPCClient(MyService.class, "tcp://localhost:5000");
        //Получение экземпляра сервиса
        MyService myService = zeroRPCClient.getService();
        //Запуск клиента
        zeroRPCClient.start();
        //Вызов метода у сервиса
        String result = myService.getStr();
        //Остановка клиента
        zeroRPCClient.shutdown();
    }
}
```  
### Больше примеров в тестах
