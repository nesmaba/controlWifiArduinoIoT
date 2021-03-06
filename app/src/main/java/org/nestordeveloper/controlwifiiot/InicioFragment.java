package org.nestordeveloper.controlwifiiot;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InicioFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InicioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InicioFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    final private static String IP_ARDUINO="192.168.4.1";
    final private static String SOLUCION="uhuru kenyatta";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final int SERVERPORT = 5000;

    InetAddress serverAddr;
    Socket socket;
    AsyncTaskConnWifiCliente asyncTaskConnWifi;

    EditText etSolucion;
    TextView tvSolucion;
    TextView tvConexion;
    TextView tvResultado;
    ImageButton btSolucion;

    private OnFragmentInteractionListener mListener;

    public InicioFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InicioFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InicioFragment newInstance(String param1, String param2) {
        InicioFragment fragment = new InicioFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_inicio, container, false);

        tvSolucion = (TextView) v.findViewById(R.id.textViewSolucion);
        btSolucion = (ImageButton) v.findViewById(R.id.imageButtonKey);
        tvConexion = (TextView) v.findViewById(R.id.textViewConexion);
        tvResultado = (TextView) v.findViewById(R.id.textViewResultado);

        // RECUPERAR ELEMENTOS VISUALES findViewById http://www.jc-mouse.net/proyectos/ejemplo-cliente-servidor-en-android
        etSolucion = (EditText) v.findViewById(R.id.editTextSolucion);

        // Nos conectamos con el Arduino mediante el WIfi
        if(asyncTaskConnWifi!=null)
            asyncTaskConnWifi.cancel(true);
        if(asyncTaskConnWifi==null || asyncTaskConnWifi.isCancelled()) {
            asyncTaskConnWifi = new AsyncTaskConnWifiCliente();
            //myATaskYW.execute(ipArduino);
            asyncTaskConnWifi.execute(IP_ARDUINO);
            // La ip del arduino será la que tiene como servidor, por defecto es 192.168.4.1
            // (tenemos que conectarnos antes a la wifi que genera el arduino como servidor
            /*
            InputMethodManager inputManager = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
                    */
        }else{
            asyncTaskConnWifi.cancel(true);
        }

        /*
        btSolucion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                WifiFragment fragmentWifi = new WifiFragment();
                fragmentTransaction.replace(R.id.frameLayoutFragments, fragmentWifi);
                fragmentTransaction.commit();
            }
        });
        */

        btSolucion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String respuesta = etSolucion.getText().toString().toLowerCase().trim();
                System.out.println("RESPUESTA "+respuesta);
                if(respuesta.equals(SOLUCION)){
                    tvResultado.setTextColor(Color.GREEN);
                    tvResultado.setText("¡¡¡¡COOOORRECTOOOO!!!!");
                    etSolucion.setBackgroundColor(Color.GREEN);
                    AsyncTaskEnviarRWifiCliente asyncTaskEnviarRWifiCliente =
                            new AsyncTaskEnviarRWifiCliente();
                    asyncTaskEnviarRWifiCliente.execute("abre");
                }else{
                    tvResultado.setTextColor(Color.RED);
                    tvResultado.setText("MAL... ¡ESTUDIA MÁS!");
                    etSolucion.setBackgroundColor(Color.RED);
                }
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            //throw new RuntimeException(context.toString()
              //      + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(socket!=null) {
            //cierra conexion
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class AsyncTaskConnWifiCliente extends AsyncTask<String,Void,String> {

        /**
         * Ventana que bloqueara la pantalla del movil hasta recibir respuesta del servidor
         * */
        ProgressBar progressBar;

        /**
         * Muestra una ventana emergente
         * */
        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            progressBar = new ProgressBar(getActivity().getApplicationContext());
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
        }

        /**
         * Se conecta al servidor y trata resultado
         * */
        @Override
        protected String doInBackground(String... values){

            try {
                //Se conecta al servidor
                serverAddr = InetAddress.getByName(IP_ARDUINO);
                Log.i("IP/TCP Client", "Connecting..."+IP_ARDUINO);
                socket = new Socket(serverAddr, SERVERPORT);
                Log.i("I/TCP Client", "Connected to server");

                // Envia peticion de cliente
                Log.i("I/TCP Client", "Send data to server");
                PrintStream output = new PrintStream(socket.getOutputStream());
                //Log.i("I/TCP Client", values[0]);
                String request = values[0]; // en values estarán los diferentes argumentos a enviar al arduino
                output.println(request);

                /*
                // Recibe respuesta del servidor y formatea a String
                Log.i("I/TCP Client", "Received data to server");
                InputStream stream = socket.getInputStream();
                byte[] lenBytes = new byte[256];
                stream.read(lenBytes,0,256);
                String received = new String(lenBytes,"UTF-8").trim();
                Log.i("I/TCP Client", "Received " + received);
                Log.i("I/TCP Client", "");
                */
                return "Conectado al Arduino";
            }catch (UnknownHostException ex) {
                Log.e("E/TCP Client1", "" + ex.getMessage());
                return ex.getMessage();
            } catch (IOException ex) {
                Log.e("E/TCP Client2", "" + ex.getMessage());
                return ex.getMessage();
            }catch (Exception e){
                Toast.makeText(getActivity().getApplicationContext(),"ERROR conexión Arduino", Toast.LENGTH_LONG).show();
                Log.e("E/TCP Client3", "" + e.getMessage());
                return e.getMessage();
            }
        }

        /**
         * Oculta ventana emergente y muestra resultado en pantalla
         * */
        @Override
        protected void onPostExecute(String value){
            progressBar.setVisibility(View.INVISIBLE);
            System.out.println("FIN ASYNC conectar");
            if(value!=null) {
                tvConexion.setTextColor(Color.GREEN);
                tvConexion.setText(value);
            }
        }
    }

    class AsyncTaskEnviarRWifiCliente extends AsyncTask<String,Void,String> {

        /**
         * Ventana que bloqueara la pantalla del movil hasta recibir respuesta del servidor
         * */
        ProgressBar progressBar;

        /**
         * Muestra una ventana emergente
         * */
        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            progressBar = new ProgressBar(getActivity().getApplicationContext());
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
        }

        /**
         * Se conecta al servidor y trata resultado
         * */
        @Override
        protected String doInBackground(String... values){
            System.out.println(socket.isConnected());

            try {
                if(socket!=null && socket.isConnected()) {
                    // Envia peticion de cliente

                    PrintStream output = new PrintStream(socket.getOutputStream());
                    String request = values[0]; // en values estarán los diferentes argumentos a enviar al arduino
                    output.println(request);
                    Log.i("I/TCP Enviar", "Enviando mensaje "+request);
                    /* Con el módulo ESP86 del Arduino no es muy estable para recibir datos de él y se queda colgado
                    // Recibe respuesta del servidor y formatea a String
                    Log.i("I/TCP Enviar", "Respuesta del servidor");
                    InputStream stream = null;

                    stream = socket.getInputStream();

                    byte[] lenBytes = new byte[256];
                    stream.read(lenBytes, 0, 256);
                    String received = new String(lenBytes, "UTF-8").trim();
                    Log.i("I/TCP Enviar", "Recibido " + received);
                    Log.i("I/TCP Enviar", "");
                    */
                    return "Enviado al Arduino";
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "No enviado al Arduino. "+e.getMessage();
            }catch (Exception e){
                Toast.makeText(getActivity().getApplicationContext(),"ERROR conexión Arduino", Toast.LENGTH_LONG).show();
                Log.e("E/TCP Enviando", "" + e.getMessage());
                return e.getMessage();
        }
            return "No enviado al Arduino.";
        }

        /**
         * Oculta ventana emergente y muestra resultado en pantalla
         * */
        @Override
        protected void onPostExecute(String value){
            progressBar.setVisibility(View.INVISIBLE);
            System.out.println("FIN ASYNC enviar");
            /*
            if(value!=null)
                etRespuestaArduino.setText(value);
            */
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
