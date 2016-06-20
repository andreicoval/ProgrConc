package ro.andrei;

import java.util.Arrays;
import java.util.concurrent.SynchronousQueue;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;

public class Channel
{
    public static void main(String args[])
    {
//generam doua arrayuri de numere intregi de lungime random
        final Integer[] S = new Integer[(int) (Math.random() * 5 + 5)];
        final Integer[] T = new Integer[(int) (Math.random() * 10 + 10)];
//populam arrayul S
        for (int i = 0; i < S.length; i++)
        {
            S[i] = (int) (Math.random() * i * 10);
        }
//populam arrayul T
        for (int i = 0; i < T.length; i++)
        {
            T[i] = (int) (Math.random() * i * 10);
        }
//afisam arrayurile ca sa vedem cu ce data am intrat in program
        System.out.println("S = " + Arrays.toString(S));
        System.out.println("T = " + Arrays.toString(T));
        System.out.println("-----------------------------");
//instantiez doua canale
//pentru comunicarea dintre S spre T
        final SynchronousQueue<String> queueForT = new SynchronousQueue<>();
//pentru comunicarea dintre T spre S
        final SynchronousQueue<String> queueForS = new SynchronousQueue<>();
//instantiez Threadul S
        Thread threadS = new Thread("S :: ") // instanta anonima de clasa Thread
        {
//override functia run() in instanta anonima de clasa Thread
            @Override
            public void run()
            {
                String threadName = Thread.currentThread().getName();

                try
                {
                    int max = findMax(S);

                    System.out.println(threadName + "max = " + max + " >> ");
//transmitem Threadului T val maxima din S si asteptam sa o preia
                    queueForT.put(String.valueOf(max)); // thread will block here
//asteptam ca Threadul T sa ne trimita inapoi val minima
                    String min_T_str = queueForS.take();

                    System.out.println(threadName + "<< min_T_str = " + min_T_str);
//incercam sa convertim valoarea trimisa de T intr-o valoare intreaga, pentru ca functia take() returneaza doar string
                    try {
                        int min_T = Integer.parseInt(min_T_str);
//                        daca conversia a trecut cu succese, inseamna ca avem minim
                        int idx = asList(S).indexOf(max);
                        S[idx] = min_T;
                        run();
                    }
                    catch(NumberFormatException ignored) {
//                        daca conversia a trecut fara succes, inseamna ca nu avem nimim deci calulele au luat sfarsit.
                        System.out.println("S = " + Arrays.toString(S));
                    }

                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }

            }
        };

        threadS.start(); // starting publisher thread

        Thread threadT = new Thread("T :: ")
        {

            public void run()
            {
                String threadName = Thread.currentThread().getName();

                try
                {
//astept valoarea maxima din Threadul S
                    int max_S = parseInt(queueForT.take()); // thread will block here
//gasesc minimul din arrayul T
                    int min_T = findMin(T);
//afisez valorile cu care incep calculele
                    System.out.println(threadName + "<< max_S = " + max_S);
                    System.out.println(threadName + "min_T = " + min_T);
//comparam daca ...
                    if(max_S > min_T)
                    {
//gasim pozitia pe care se afla minimul din Threadul T
                        int idx = asList(T).indexOf(min_T);
//inlocuim val minima din T cu val max din S
                        T[idx] = max_S;
//trimitem in Threadul S val minima din T
                        queueForS.put(String.valueOf(min_T));
                        run();
                    }
                    else
                    {
//cand nu mai sunt val mai mici in T, afisam comentariul "..."
                        queueForS.put("no more minimum");
                        System.out.println("---------------------");
                        System.out.println("T = " + Arrays.toString(T));
                    }
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }

            }
        };

        threadT.start(); // starting consumer thread

    }
//gasim max dintr-un array
    static int findMax(Integer[] array)
    {
        int max = array[0];

        for (int i = 1; i < array.length; i++)
        {
            max = Math.max(max, array[i]);
        }

        return max;
    }
//gasim dintr-un array
    static int findMin(Integer[] array)
    {
        int min = array[0];

        for (int i = 1; i < array.length; i++)
        {
            min = Math.min(min, array[i]);
        }

        return min;
    }

}