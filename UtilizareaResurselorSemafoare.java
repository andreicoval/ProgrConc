package ro.andrei;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

//Aceasta clasa ruleaza in continuu. Ea trebuie oprita manual. Ea simuleaza un caz cand magazinul opereaza non-stop

public class Resources
{
    private final static int clientsNumber = 20;

    public static void main(String args[]) throws InterruptedException
    {
        Store store = new Store();
        Thread[] clientThreads = new Thread[clientsNumber];
//instantiem clientii si populam array-ul de thread-uri
        for(int i = 0; i < clientsNumber; i++)
        {
            ClientAction clientAction = new ClientAction(store);
            Thread clientThread = new Thread(clientAction);

//            nume personalizat, pentru afisare mai comoda
            clientThread.setName(i + "---Client");

//            pornim threadu / clientul
            clientThread.start();

//            salvam threadul in lista de fire
            clientThreads[i] = clientThread;
        }

        for (Thread t : clientThreads)
        {
            t.join();
        }
    }
}

//Clasa ClientAction reprezinta actiunea pe care o indeplineste un client
//Implementam Runnable, pentru ca "Thread"-ul primeste ca parametru o implementare a interfetei Runnable

class ClientAction implements Runnable
{
//cream o variabile store de tip Store care va fi referinta la Store-ul din functia main()
    private final Store store;
//constructor, functie apelata automat la instantierea unui obiect de acest tip (ClientAction)
    public ClientAction(Store store)
    {
        this.store = store;
    }
//implementez functia run() a interfetei Runnable
    @Override
    public void run()
    {
        try
        {
//ia din magazin un nr aleator de elemente de la 1 la 4
            String[] itemsFromStore = store.getItems((int) (1 + Math.random() * 4));
//afisam elementele luate de catre un client
            System.out.println("take <<-- " + Thread.currentThread().getName() + " :: " + Arrays.toString(itemsFromStore));
//spunem threadului curent sa faca o pauza
            Thread.sleep(500 + (int) (Math.random() * 2000));

//afisam elementele pe care le punem inapoi in magazin
            System.out.println("put  -->> " + Thread.currentThread().getName() + " :: " + Arrays.toString(itemsFromStore));
            store.putItem(itemsFromStore);

//clientii revin la magazin peste un timp aleator
            Thread.sleep(500 + (int) (Math.random() * 1000));
            run();
        }
//trateaza exceptiile
        catch (InterruptedException exception)
        {
//afiseaza detaliile exceptiei
            exception.printStackTrace();
        }
    }
}

//clasa Store expune 2 functii:
//getItem - primeste elemente
//putItem - returneaza elemente

class Store
{
    private final int objectsNumber = 10;

//colectia de elemente care pot fi luate
    private String[] items = new String[objectsNumber];
//lista de stari a elementelor luate(luate=true; returnate=false)
    private boolean[] used  = new boolean[objectsNumber];
//instantiem semaforul si setam nr maxim de permisiuni = objectsNumber
    private final Semaphore available = new Semaphore(objectsNumber, true);

//constructorul: populeaza lista de obiecte disponibile in magazin
    public Store()
    {
        for(int i = 0; i < objectsNumber; i++)
        {
            items[i] = "Object" + i;
        }
    }
//functia returneaza un nr de obiecte din magazin
    public String[] getItems(int nrOfItemsToGive) throws InterruptedException
    {
        available.acquire(nrOfItemsToGive);

        String[] arr = new String[nrOfItemsToGive];

        for (int j = 0; j < nrOfItemsToGive; j++)
        {
            arr[j] = getNextAvailableItem();
        }

        return arr;
    }
//functia pune inapoi obiectele in magazin
    public void putItem(String[] returnedObjects)
    {
        for (String item : returnedObjects)
        {
            if (markAsUnused(item))
            {
                available.release();
            }
        }

    }

    // Not a particularly efficient data structure; just for demo

//gaseste un obiect disponibil si-l marcheaza ca fiind dat unui client
    protected synchronized String getNextAvailableItem()
    {
        for (int i = 0; i < objectsNumber; ++i)
        {
            if (!used[i])
            {
                used[i] = true;
                return items[i];
            }
        }
        return null; // not reached
    }
//il marcheaza ca fiind returnat si disponibil pentru urmatorul client
    protected synchronized boolean markAsUnused(String item)
    {
        for (int i = 0; i < objectsNumber; ++i)
        {
            if (item == items[i])
            {
                if (used[i])
                {
                    used[i] = false;
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
        return false;
    }
}