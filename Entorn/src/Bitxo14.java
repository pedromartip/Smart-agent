package agents;

// Exemple de Bitxo
public class Bitxo14 extends Agent {

    public int repetir = 5;
    public int impactesRebutsAux = 0;

    static final int PARET = 0;
    static final int BITXO = 1;
    static final int RES = -1;
    static final int MENJAR = 100;
    static final int ESCUT = 5;

    static final int ESQUERRA = 0;
    static final int CENTRAL = 1;
    static final int DRETA = 2;

    Estat estat;

    public Bitxo14(Agents pare) {
        super(pare, "Eren Orr", "imatges/img.gif");
    }

    @Override
    public void inicia() {
        // atributsAgents(v,w,dv,av,ll,es,hy)
        int cost = atributsAgent(5, 6, 600, 30, 23, 5, 5);
        System.out.println("Cost total:" + cost);

        // Inicialització de variables que utilitzaré al meu comportament
    }

    @Override
    public void avaluaComportament() {

        estat = estatCombat();
        if (repetir != 0) {
            System.out.println("repito");
            repetir--;
        } else {

            atura();
            estat = estatCombat();
            filtrarObjecte(0);
            
            endavant();

            //DETECTAM QUE UN ENEMIC ENS HA DISPARAT --> ESQUIVAM
            if (estat.llançamentEnemicDetectat) {
                esquivar();
            } //SI REBEM UN TIR, ESCAPE D'EMERGENCIA

            if ((impactesRebutsAux < estat.impactesRebuts) && !estat.hiperEspaiActiu) {
                impactesRebutsAux = estat.impactesRebuts;
                hyperespai();
            } //VEIM UN ENEMIC --> ACTUAM DEPENENT DE LA SEVA DISTANCIA I POSICIO


            //ESTEIM EN COL·LISIO I VOLEM TORNAR A PUESTO
            if (estat.enCollisio && !estat.hiperEspaiActiu) {
                hyperespai();
            }
            
            //EVITAM LA PARET
            if (detectaParet(60)) {
                System.out.println("paret a 90");
                onGirar();
                repetir = 5;
            }
            
            if (detectaParet(10)) {
                System.out.println("paret a 10");
                enrere();

                if (estat.distanciaVisors[0] < estat.distanciaVisors[2]) {
                    esquerra();
                } else {
                    dreta();
                }
                repetir = 6;
            }

            
            
            if (estat.veigAlgunEnemic) {
                filtrarObjecte(1);
            }
            
            
        }
    }

    //Si es 0 miram paret, si es 1 miram bitxo
    //Saber d'on ve i cap on anar
    public void onGirar() {

        //CAS DE QUE VEU UN OBJECTE A CADA COSTAT
        if (estat.objecteVisor[0] == PARET && estat.objecteVisor[2] == PARET) {
            //CAS EN QUE L'OBJECTE DE L'ESQUERRA ESTIGUI MÉS APROP QUE EL DE LA DRETA      
            if (estat.distanciaVisors[0] < 15 && estat.distanciaVisors[2] < 15) {
                esquerra();
                enrere();
            }else if ((estat.distanciaVisors[0] < estat.distanciaVisors[2])) {
                dreta();

            } else if (estat.distanciaVisors[0] >= estat.distanciaVisors[2]) { //CAS EN QUE L'OBJECTE DE LA DRETA ESTIGUI MÉS APROP QUE EL DE L'ESQUERRA
                esquerra();
            }

            //CAS EN QUE ÚNICAMENT DETECTA L'OBJECTE A LA DRETA
        } else if (estat.objecteVisor[0] == PARET) {
            dreta();

            //CAS EN QUE ÚNICAMENT DETECTA L'OBJECTE A L'ESQUERRA
        } else {
            esquerra();
        }
    }

    //Diu si hi ha paret davant del visor a "dist" distancia.
    public boolean detectaParet(int dist) {
        boolean hihaParet = false;
        if ((estat.distanciaVisors[0] <= dist && estat.objecteVisor[0] == 0)
                || (estat.distanciaVisors[1] <= (dist + 30) && estat.objecteVisor[1] == 0)
                || (estat.distanciaVisors[2] <= dist && estat.objecteVisor[2] == 0)) {
            hihaParet = true;
        }

        return hihaParet;
    }

    public void esquivar() {
        int dist = 9999;
        if (estat.distanciaLlançamentEnemic < 10) {
            if (!estat.escutActivat) {
                activaEscut();
            }
        } else if (estat.distanciaLlançamentEnemic > 10) {
            for (int i = 0; i < estat.numObjectes; i++) {
                if (estat.objectes[i].agafaTipus() == Estat.AGENT && estat.objectes[i].agafaDistancia() < dist) {
                    if (estat.objectes[i].agafaSector() == 2) {
                        esquerra();
                        repetir = 3;
                    } else if (estat.objectes[i].agafaSector() == 3) {
                        dreta();
                        repetir = 3;
                    }
                }
            }
        }
    }

    public void filtrarObjecte(int n) { //0 si recurs, 1 si enemic
        int dist = 9999;
        for (int i = 0; i < estat.numObjectes; i++) {

            if (n == 0) { //INTERPRETAM RECURS

                /* CONDICIONS:
                - Sigui escut o menjar nostre
                - Estigui mes aprop
                - Estigui dins els sectors de visió 2 i 3 */
                if (((estat.objectes[i].agafaTipus() == (MENJAR + estat.id) || estat.objectes[i].agafaTipus() == ESCUT)
                        && estat.objectes[i].agafaDistancia() < dist)
                        && (estat.objectes[i].agafaSector() == 2 || estat.objectes[i].agafaSector() == 3)) {
                    mira(estat.objectes[i]);
                    dist = estat.objectes[i].agafaDistancia();
                } /* CONDICIONS:
                - Sigui un menjar enemic (id = 100)
                - Es el que esta més aprop
                - Estigui dins els sectors 2 i 3 */ else if (estat.objectes[i].agafaTipus() == MENJAR && estat.objectes[i].agafaDistancia() < dist) {
                    if (estat.llançaments != 0 && estat.objectes[i].agafaDistancia() <= 150
                            && (estat.objectes[i].agafaSector() == 2 || estat.objectes[i].agafaSector() == 3)) {
                        mira(estat.objectes[i]);
                        llança();
                    } else {
                        if (estat.objectes[i].agafaSector() == 2) {
                            dreta();
                        } else {
                            esquerra();
                        }
                    }
                }

            } else if (n == 1) { //INTERPRETAM ENEMIC

                /*CONDICIONS:
                - Sigui un bitxo i es el que esta mes aprop
                - Estigui entre els sectors 2 i 3
                - Estigui a una distància igual o menor a 150 (així el tir té més 
                  probabilitats d'encertar perque es un objecte en moviment).  */
                if (estat.objectes[i].agafaTipus() == BITXO) {
                    if ((estat.objectes[i].agafaSector() == 2 || estat.objectes[i].agafaSector() == 3)) {

                        dist = estat.objectes[i].agafaDistancia();

                        if (dist <= 150) {
                            mira(estat.objectes[i]);
                            llança();
                        }
                    }

                    if (estat.objectes[i].agafaSector() == 2) {
                        esquerra();
                        repetir = 3;
                    } else if (estat.objectes[i].agafaSector() == 3) {
                        dreta();
                        repetir = 3;
                    }
                }
            }
        }
    }
}
