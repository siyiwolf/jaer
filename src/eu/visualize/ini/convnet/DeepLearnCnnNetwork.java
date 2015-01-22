/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.visualize.ini.convnet;

import java.io.File;
import static net.sf.jaer.eventprocessing.EventFilter.log;

/**
 * Simple convolutional neural network (CNN) data structure to hold CNN from
 * Matlab DeepLearnToolbox
 *
 * @author tobi
 */
public class DeepLearnCnnNetwork {

    int nLayers;
    String netname;
    String notes;
    String dob;
    String nettype;
    Layer[] layers;
    OutputLayer outputLayer;

    public class Layer {

        int index;

    }

    public class ConvLayer extends Layer {

        int inputMaps;
        int outputMaps;
        int kernelSize;
        float[] biases;
        float[] kernels;

        public String toString() {
            return String.format("index=%d CNN   layer; inputMaps=%d outputMaps=%d kernelSize=%d biases=float[%d] kernels=float[%d]\n",
                    index, inputMaps, outputMaps, kernelSize, biases == null ? 0 : biases.length, kernels == null ? 0 : kernels.length);
        }

    }

    public class InputLayer extends Layer {

        int dimx;
        int dimy;
        int nUnits;

        public String toString() {
            return String.format("index=%d Input layer; dimx=%d dimy=%d nUnits=%d\n",
                    index, dimx, dimy, nUnits);
        }
    }

    public class SubsamplingLayer extends Layer {

        int averageOver;
        float[] biases;

        public String toString() {
            return String.format("index=%d Subsamp layer; averageOver=%d biases=float[%d]\n",
                    index, averageOver, biases == null ? 0 : biases.length);
        }
    }

    public class OutputLayer {

        float[] outputBias;
        float[] outputWeights;
        
        public String toString(){
            return String.format("Output: bias=float[%d] outputWeights=float[%d] \n", outputBias.length, outputWeights.length);
        }
    }

    public void loadFromXMLFile(File f) {
        EasyXMLReader networkReader = new EasyXMLReader(f);
        if (!networkReader.hasFile()) {
            log.warning("No file for reader; file=" + networkReader.getFile());
            return;
        }

        netname = networkReader.getRaw("name");
        notes = networkReader.getRaw("notes");
        dob = networkReader.getRaw("dob");
        nettype = networkReader.getRaw("type");
        if (!nettype.equals("cnn")) {
            log.warning("network type is not cnn");
        }
        nLayers = networkReader.getNodeCount("Layer");
        layers = new Layer[nLayers];

        for (int i = 0; i < nLayers; i++) {
            EasyXMLReader layerReader = networkReader.getNode("Layer", i);
            int index = layerReader.getInt("index");
            String type = layerReader.getRaw("type");
            switch (type) {
                case "i": {
                    InputLayer l = new InputLayer();
                    layers[index] = l;
                    l.dimx = layerReader.getInt("dimx");
                    l.dimy = layerReader.getInt("dimy");
                    l.nUnits = layerReader.getInt("nUnits");
                }
                break;
                case "c": {
                    ConvLayer l = new ConvLayer();
                    layers[index] = l;
                    l.inputMaps = layerReader.getInt("inputMaps");
                    l.outputMaps = layerReader.getInt("outputMaps");
                    l.kernelSize = layerReader.getInt("kernelSize");
                    l.biases = layerReader.getBase64FloatArr("biases");
                    l.kernels = layerReader.getBase64FloatArr("kernels");
                }
                break;
                case "s": {
                    SubsamplingLayer l = new SubsamplingLayer();
                    layers[index] = l;
                    l.averageOver = layerReader.getInt("averageOver");
                    l.biases = layerReader.getBase64FloatArr("biases");

                }
                break;
            }
        }
        OutputLayer outputLayer = new OutputLayer();
        outputLayer.outputBias = networkReader.getBase64FloatArr("outputBias");
        outputLayer.outputWeights = networkReader.getBase64FloatArr("outputWeights");
        log.info(toString());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DeepLearnCnnNetwork: \n");
        sb.append(String.format("name=%s, dob=%s, type=%s\nnotes=%s\n", netname, dob, nettype, notes));
        sb.append(String.format("nLayers=%d\n", nLayers));
        for(Layer l:layers){
            sb.append((l==null?"null layer":l.toString())+"\n");
        }
        sb.append(outputLayer==null?"null outputLayer":toString());
        return sb.toString();

    }

}
