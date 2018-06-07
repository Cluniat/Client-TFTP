import java.io.*;
import java.net.*;

class Client {

    private DatagramSocket socket;
    private View vue;

    Client(View p_vue) {
        vue = p_vue;
    }

    /*
     * Constrution du paquet WRQ
     *
     * @param : int valeur
     * Si valeur = 1, c'est un WRQ.
     */
    private byte[] paquetWRRQ(String nomFichier, int valeur) {
        byte[] buffer = new byte[516];
        byte opCode[] = new byte[] {0, (byte)valeur};
        byte fichier[] = nomFichier.getBytes();
        byte bourrage[] = new byte[] {0};
        byte type[] = "octet".getBytes();

        int index = 0;
        System.arraycopy(opCode, 0, buffer, index, 2);
        index += 2;
        System.arraycopy(fichier, 0, buffer, index, fichier.length);
        index += fichier.length;
        System.arraycopy(bourrage, 0, buffer, index, 1);
        index++;
        System.arraycopy(type, 0, buffer, index, type.length);
        index += type.length;
        System.arraycopy(bourrage, 0, buffer, index, 1);
        return buffer;
    }

    int sendFile(String p_path, String p_nomFichier, String p_nomDistant, String p_adresse, int p_port) {

        DatagramPacket wrq;
        DatagramPacket ack;
        FileInputStream fichier = null;
        int i = 0, j = 0, ttl = 0;

        try {
            //Ouverture socket
            InetAddress ipServeur = InetAddress.getByName(p_adresse);
            socket = new DatagramSocket();

            //Envoi du WRQ
            byte[] bufferEnvoi = paquetWRRQ(p_nomDistant, Commun.TypePaquet.WRQ.code);
            System.out.println(Commun.TypePaquet.WRQ.code);
            wrq = new DatagramPacket(bufferEnvoi, bufferEnvoi.length, ipServeur, p_port);
            socket.send(wrq);

            //Reception ACK
            byte[] bufferReception = new byte[4];
            ack = new DatagramPacket(bufferReception, bufferReception.length);
            socket.receive(ack);

            //Si c'est un ACK, on peut envoyer le fichier
            if(bufferReception[1] == Commun.TypePaquet.ACK.code)
            {
                int portServeur = ack.getPort();
                vue.getTxtInfoArea().append("Serveur - "+ ipServeur +":"+ portServeur +"\n");
                vue.repaint();
                bufferEnvoi = new byte[516];
                byte[] donneesFichier = new byte[512];

                //Ouverture du fichier
                try{
                    fichier = new FileInputStream(p_path+"/"+p_nomFichier);
                    vue.getTxtInfoArea().append("Ouverture du fichier réussi\n");
                    vue.repaint();
                }
                catch (FileNotFoundException e) //Fichier non trouvé ou accès refusé.
                {
                    vue.getTxtInfoArea().append("Erreur -1 : Echec de l'ouverture du fichier\n");
                    return -1;
                }

                //Tant que le read n'a pas lu tout le fichier
                while(fichier.read(donneesFichier)>0)
                {
                    bufferEnvoi[0] = (byte) 0;
                    bufferEnvoi[1] = (byte) Commun.TypePaquet.DATA.code;
                    bufferEnvoi[2] = (byte) j;
                    bufferEnvoi[3] = (byte) (i+1);

                    //Copie du buffer fichier après l'entete de 4 bytes
                    System.arraycopy(donneesFichier, 0, bufferEnvoi, 4, donneesFichier.length);

                    //Tant que l'ACK n'est pas le bon on envoi le paquet
                    // ou que ce n'est pas un ACK
                    int verifAck = -1;
                    DatagramPacket donnees = new DatagramPacket(bufferEnvoi, bufferEnvoi.length, ipServeur, portServeur);
                    while(verifAck != (i+1)
                            || bufferReception[1] != Commun.TypePaquet.ACK.code) {
                        //Envoi du paquet
                        socket.send(donnees);
                        bufferReception = new byte[516];
                        //Réception ack
                        ack = new DatagramPacket(bufferReception, bufferReception.length);
                        socket.receive(ack);
                        verifAck = bufferReception[3];
                        if(verifAck < 0)
                            verifAck = 256 - Math.abs(bufferReception[3]);

                        ttl++;
                        if (ttl > 30) {
                            vue.getTxtInfoArea().append("Erreur -2 : Dépassement de délai\n");
                            return -2;
                        }
                    }

                    ttl = 0;
                    i++;
                    if (i == 255) {
                        j++;
                        i = -1;
                        //Laisser -1 absolument sinon les paquets
                        //multiple de 256 ne sont pas envoyés
                    }
                    bufferEnvoi = new byte[516];
                    donneesFichier = new byte[512];
                }

                //Envoi d'un dernier paquet vide pour les fichier multiple de 512
                bufferEnvoi = new byte[4];
                bufferEnvoi[0] = (byte) 0;
                bufferEnvoi[1] = (byte) Commun.TypePaquet.DATA.code;
                bufferEnvoi[2] = (byte) j;
                bufferEnvoi[3] = (byte) (i+1);
                DatagramPacket donneesDernierPacket = new DatagramPacket(bufferEnvoi, bufferEnvoi.length, ipServeur, portServeur);
                socket.send(donneesDernierPacket);
            }
            else if (bufferReception[1] == Commun.TypePaquet.ERROR.code)
            {
                vue.getTxtInfoArea().append(
                        Commun.TypeErreurServeur.getStringFromValue(bufferReception[3]).libelle
                                +"\n");
                return bufferReception[3];
            }
            vue.getTxtInfoArea().append("Fichier envoyé\n");
            assert fichier != null;
            fichier.close();
        }
        catch (UnknownHostException e) {
            vue.getTxtInfoArea().append("Erreur -3 : IP indéterminée\n");
            return -3;
        }
        catch (SocketException e1) {
            vue.getTxtInfoArea().append("Erreur -4 : Problème de création ou d'accès au socket\n");
            return -4;
        }
        catch (IOException e1) {
            vue.getTxtInfoArea().append("Erreur -5 : Problème réseau\n");
            return -5;
        }
        catch (Exception e) {
            vue.getTxtInfoArea().append("Erreur -6 : Problème inconnu\n");
            return -6;
        }finally {
            socket.close();
        }
        return 0;
    }



}
