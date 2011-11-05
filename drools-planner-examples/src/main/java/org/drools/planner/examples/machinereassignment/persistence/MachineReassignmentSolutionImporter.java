/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.examples.machinereassignment.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.drools.planner.core.solution.Solution;
import org.drools.planner.examples.common.persistence.AbstractTxtSolutionImporter;
import org.drools.planner.examples.machinereassignment.domain.MachineReassignment;
import org.drools.planner.examples.machinereassignment.domain.MrBalancePenalty;
import org.drools.planner.examples.machinereassignment.domain.MrMachineCapacity;
import org.drools.planner.examples.machinereassignment.domain.MrGlobalPenaltyInfo;
import org.drools.planner.examples.machinereassignment.domain.MrLocation;
import org.drools.planner.examples.machinereassignment.domain.MrMachine;
import org.drools.planner.examples.machinereassignment.domain.MrMachineMoveCost;
import org.drools.planner.examples.machinereassignment.domain.MrNeighborhood;
import org.drools.planner.examples.machinereassignment.domain.MrProcess;
import org.drools.planner.examples.machinereassignment.domain.MrProcessAssignment;
import org.drools.planner.examples.machinereassignment.domain.MrProcessRequirement;
import org.drools.planner.examples.machinereassignment.domain.MrResource;
import org.drools.planner.examples.machinereassignment.domain.MrService;
import org.drools.planner.examples.machinereassignment.domain.MrServiceDependency;

public class MachineReassignmentSolutionImporter extends AbstractTxtSolutionImporter {

    public static void main(String[] args) {
        new MachineReassignmentSolutionImporter().convertAll();
    }

    public MachineReassignmentSolutionImporter() {
        super(new MachineReassignmentDaoImpl());
    }

    @Override
    public boolean acceptInputFile(File inputFile) {
        return super.acceptInputFile(inputFile) && inputFile.getName().startsWith("model_");
    }

    public TxtInputBuilder createTxtInputBuilder() {
        return new MachineReassignmentInputBuilder();
    }

    public class MachineReassignmentInputBuilder extends TxtInputBuilder {

        private MachineReassignment machineReassignment;

        private int resourceListSize;
        private List<MrResource> resourceList;
        private List<MrService> serviceList;
        private List<MrMachine> machineList;
        private int processListSize;
        private List<MrProcess> processList;

        public Solution readSolution() throws IOException {
            machineReassignment = new MachineReassignment();
            machineReassignment.setId(0L);
            readResourceList();
            readMachineList();
            readServiceList();
            readProcessList();
            readBalancePenaltyList();
            readGlobalPenaltyInfo();
            readProcessAssignmentList();
            logger.info("MachineReassignment with {} resources, {} neighborhoods, {} locations, {} machines," +
                    " {} services, {} processes and {} balancePenalties.",
                    new Object[]{machineReassignment.getResourceList().size(),
                            machineReassignment.getNeighborhoodList().size(),
                            machineReassignment.getLocationList().size(),
                            machineReassignment.getMachineList().size(),
                            machineReassignment.getServiceList().size(),
                            machineReassignment.getProcessList().size(),
                            machineReassignment.getBalancePenaltyList().size()});
            BigInteger possibleSolutionSize = BigInteger.valueOf(machineReassignment.getMachineList().size()).pow(
                    machineReassignment.getProcessList().size());
            String flooredPossibleSolutionSize = "10^" + (possibleSolutionSize.toString().length() - 1);
            logger.info("MachineReassignment with flooredPossibleSolutionSize ({}) and possibleSolutionSize({}).",
                    flooredPossibleSolutionSize, possibleSolutionSize);
            return machineReassignment;
        }

        private void readResourceList() throws IOException {
            resourceListSize = readIntegerValue();
            resourceList = new ArrayList<MrResource>(resourceListSize);
            long resourceId = 0L;
            for (int i = 0; i < resourceListSize; i++) {
                String line = readStringValue();
                String[] lineTokens = splitBySpace(line, 2);
                MrResource resource = new MrResource();
                resource.setId(resourceId);
                resource.setTransientlyConsumed(parseBooleanFromNumber(lineTokens[0]));
                resource.setWeight(Integer.parseInt(lineTokens[1]));
                resourceList.add(resource);
                resourceId++;
            }
            machineReassignment.setResourceList(resourceList);
        }

        private void readMachineList() throws IOException {
            int machineListSize = readIntegerValue();
            List<MrNeighborhood> neighborhoodList = new ArrayList<MrNeighborhood>(machineListSize);
            Map<Long, MrNeighborhood> idToNeighborhoodMap = new HashMap<Long, MrNeighborhood>(machineListSize);
            List<MrLocation> locationList = new ArrayList<MrLocation>(machineListSize);
            Map<Long, MrLocation> idToLocationMap = new HashMap<Long, MrLocation>(machineListSize);
            machineList = new ArrayList<MrMachine>(machineListSize);
            long machineId = 0L;
            List<MrMachineCapacity> machineCapacityList = new ArrayList<MrMachineCapacity>(machineListSize * resourceListSize);
            long machineCapacityId = 0L;
            List<MrMachineMoveCost> machineMoveCostList = new ArrayList<MrMachineMoveCost>(machineListSize * machineListSize);
            long machineMoveCostId = 0L;
            // 2 phases because service dependencies are not in low to high order
            for (int i = 0; i < machineListSize; i++) {
                MrMachine machine = new MrMachine();
                machine.setId(machineId);
                machineList.add(machine);
                machineId++;
            }
            for (int i = 0; i < machineListSize; i++) {
                MrMachine machine = machineList.get(i);
                String line = readStringValue();
                int moveCostOffset = 2 + (resourceListSize * 2);
                String[] lineTokens = splitBySpace(line, moveCostOffset + machineListSize);
                long neighborhoodId = Long.parseLong(lineTokens[0]);
                MrNeighborhood neighborhood = idToNeighborhoodMap.get(neighborhoodId);
                if (neighborhood == null) {
                    neighborhood = new MrNeighborhood();
                    neighborhood.setId(neighborhoodId);
                    neighborhoodList.add(neighborhood);
                    idToNeighborhoodMap.put(neighborhoodId, neighborhood);
                }
                machine.setNeighborhood(neighborhood);
                long locationId = Long.parseLong(lineTokens[1]);
                MrLocation location = idToLocationMap.get(locationId);
                if (location == null) {
                    location = new MrLocation();
                    location.setId(locationId);
                    locationList.add(location);
                    idToLocationMap.put(locationId, location);
                }
                machine.setLocation(location);
                Map<MrResource, MrMachineCapacity> machineCapacityMap
                        = new LinkedHashMap<MrResource, MrMachineCapacity>(resourceListSize);
                for (int j = 0; j < resourceListSize; j++) {
                    MrMachineCapacity machineCapacity = new MrMachineCapacity();
                    machineCapacity.setId(machineCapacityId);
                    machineCapacity.setMachine(machine);
                    machineCapacity.setResource(resourceList.get(j));
                    machineCapacity.setMaximumCapacity(Integer.parseInt(lineTokens[2 + j]));
                    machineCapacity.setSafetyCapacity(Integer.parseInt(lineTokens[2 + resourceListSize + j]));
                    machineCapacityList.add(machineCapacity);
                    machineCapacityMap.put(resourceList.get(j), machineCapacity);
                    machineCapacityId++;
                }
                machine.setMachineCapacityMap(machineCapacityMap);
                Map<MrMachine, MrMachineMoveCost> machineMoveCostMap
                        = new LinkedHashMap<MrMachine, MrMachineMoveCost>(machineListSize);
                for (int j = 0; j < machineListSize; j++) {
                    MrMachineMoveCost machineMoveCost = new MrMachineMoveCost();
                    machineMoveCost.setId(machineMoveCostId);
                    machineMoveCost.setFromMachine(machine);
                    MrMachine toMachine = machineList.get(j);
                    machineMoveCost.setToMachine(toMachine);
                    machineMoveCost.setMoveCost(Integer.parseInt(lineTokens[moveCostOffset + j]));
                    machineMoveCostList.add(machineMoveCost);
                    machineMoveCostMap.put(toMachine, machineMoveCost);
                    machineMoveCostId++;
                }
                machine.setMachineMoveCostMap(machineMoveCostMap);
            }
            machineReassignment.setNeighborhoodList(neighborhoodList);
            machineReassignment.setLocationList(locationList);
            machineReassignment.setMachineList(machineList);
            machineReassignment.setMachineCapacityList(machineCapacityList);
            machineReassignment.setMachineMoveCostList(machineMoveCostList);
        }

        private void readServiceList() throws IOException {
            int serviceListSize = readIntegerValue();
            serviceList = new ArrayList<MrService>(serviceListSize);
            long serviceId = 0L;
            // 2 phases because service dependencies are not in low to high order
            for (int i = 0; i < serviceListSize; i++) {
                MrService service = new MrService();
                service.setId(serviceId);
                serviceList.add(service);
                serviceId++;
            }
            List<MrServiceDependency> serviceDependencyList = new ArrayList<MrServiceDependency>(serviceListSize * 5);
            long serviceDependencyId = 0L;
            for (int i = 0; i < serviceListSize; i++) {
                MrService service = serviceList.get(i);
                String line = readStringValue();
                String[] lineTokens = splitBySpace(line);
                service.setLocationSpread(Integer.parseInt(lineTokens[0]));
                int serviceDependencyListSize = Integer.parseInt(lineTokens[1]);
                for (int j = 0; j < serviceDependencyListSize; j++) {
                    MrServiceDependency serviceDependency = new MrServiceDependency();
                    serviceDependency.setId(serviceDependencyId);
                    serviceDependency.setFromService(service);
                    int toServiceIndex = Integer.parseInt(lineTokens[2 + j]);
                    if (toServiceIndex >= serviceList.size()) {
                        throw new IllegalArgumentException("Service with id (" + serviceId
                                + ") has a non existing toServiceIndex (" + toServiceIndex + ").");
                    }
                    MrService toService = serviceList.get(toServiceIndex);
                    serviceDependency.setToService(toService);
                    serviceDependencyList.add(serviceDependency);
                    serviceDependencyId++;
                }
                int numberOfTokens = 2 + serviceDependencyListSize;
                if (lineTokens.length != numberOfTokens) {
                    throw new IllegalArgumentException("Read line (" + line + ") has " + lineTokens.length
                            + " tokens but is expected to contain " + numberOfTokens + " tokens separated by space.");
                }
            }
            machineReassignment.setServiceList(serviceList);
            machineReassignment.setServiceDependencyList(serviceDependencyList);
        }

        private void readProcessList() throws IOException {
            processListSize = readIntegerValue();
            processList = new ArrayList<MrProcess>(processListSize);
            long processId = 0L;
            List<MrProcessRequirement> processRequirementList = new ArrayList<MrProcessRequirement>(processListSize * resourceListSize);
            long processRequirementId = 0L;
            for (int i = 0; i < processListSize; i++) {
                String line = readStringValue();
                String[] lineTokens = splitBySpace(line, 2 + resourceListSize);
                MrProcess process = new MrProcess();
                process.setId(processId);
                int serviceIndex = Integer.parseInt(lineTokens[0]);
                if (serviceIndex >= serviceList.size()) {
                    throw new IllegalArgumentException("Process with id (" + processId
                            + ") has a non existing serviceIndex (" + serviceIndex + ").");
                }
                MrService service = serviceList.get(serviceIndex);
                process.setService(service);
                Map<MrResource, MrProcessRequirement> processRequirementMap
                        = new LinkedHashMap<MrResource, MrProcessRequirement>(resourceListSize);
                for (int j = 0; j < resourceListSize; j++) {
                    MrProcessRequirement processRequirement = new MrProcessRequirement();
                    processRequirement.setId(processRequirementId);
                    processRequirement.setProcess(process);
                    processRequirement.setResource(resourceList.get(j));
                    processRequirement.setUsage(Integer.parseInt(lineTokens[1 + j]));
                    processRequirementList.add(processRequirement);
                    processRequirementMap.put(resourceList.get(j), processRequirement);
                    processRequirementId++;
                }
                process.setProcessRequirementMap(processRequirementMap);
                process.setMoveCost(Integer.parseInt(lineTokens[1 + resourceListSize]));
                processList.add(process);
                processId++;
            }
            machineReassignment.setProcessList(processList);
            machineReassignment.setProcessRequirementList(processRequirementList);
        }

        private void readBalancePenaltyList() throws IOException {
            int balancePenaltyListSize = readIntegerValue();
            List<MrBalancePenalty> balancePenaltyList = new ArrayList<MrBalancePenalty>(balancePenaltyListSize);
            long balancePenaltyId = 0L;
            for (int i = 0; i < balancePenaltyListSize; i++) {
                String line = readStringValue();
                String[] lineTokens = splitBySpace(line, 3);
                MrBalancePenalty balancePenalty = new MrBalancePenalty();
                balancePenalty.setId(balancePenaltyId);
                int originResourceIndex = Integer.parseInt(lineTokens[0]);
                if (originResourceIndex >= resourceListSize) {
                    throw new IllegalArgumentException("BalancePenalty with id (" + balancePenaltyId
                            + ") has a non existing originResourceIndex (" + originResourceIndex + ").");
                }
                balancePenalty.setOriginResource(resourceList.get(originResourceIndex));
                int targetResourceIndex = Integer.parseInt(lineTokens[1]);
                if (targetResourceIndex >= resourceListSize) {
                    throw new IllegalArgumentException("BalancePenalty with id (" + balancePenaltyId
                            + ") has a non existing targetResourceIndex (" + targetResourceIndex + ").");
                }
                balancePenalty.setTargetResource(resourceList.get(targetResourceIndex));
                balancePenalty.setMultiplicand(Integer.parseInt(lineTokens[2]));
                // Read a new line (weird in the input definition)
                balancePenalty.setWeight(readIntegerValue());
                balancePenaltyList.add(balancePenalty);
                balancePenaltyId++;
            }
            machineReassignment.setBalancePenaltyList(balancePenaltyList);
        }

        private void readGlobalPenaltyInfo() throws IOException {
            MrGlobalPenaltyInfo globalPenaltyInfo = new MrGlobalPenaltyInfo();
            globalPenaltyInfo.setId(0L);
            String line = readStringValue();
            String[] lineTokens = splitBySpace(line, 3);
            globalPenaltyInfo.setProcessMovePenaltyCost(Integer.parseInt(lineTokens[0]));
            globalPenaltyInfo.setServiceMovePenaltyCost(Integer.parseInt(lineTokens[1]));
            globalPenaltyInfo.setMachineMovePenaltyCost(Integer.parseInt(lineTokens[2]));
            machineReassignment.setGlobalPenaltyInfo(globalPenaltyInfo);
        }

        private void readProcessAssignmentList() {
            String line = readOriginalProcessAssignmentLine();
            String[] lineTokens = splitBySpace(line, processListSize);
            List<MrProcessAssignment> processAssignmentList = new ArrayList<MrProcessAssignment>(processListSize);
            long processAssignmentId = 0L;
            for (int i = 0; i < processListSize; i++) {
                MrProcessAssignment processAssignment = new MrProcessAssignment();
                processAssignment.setId(processAssignmentId);
                processAssignment.setProcess(processList.get(i));
                int machineIndex = Integer.parseInt(lineTokens[i]);
                if (machineIndex >= machineList.size()) {
                    throw new IllegalArgumentException("ProcessAssignment with id (" + processAssignmentId
                            + ") has a non existing machineIndex (" + machineIndex + ").");
                }
                processAssignment.setOriginalMachine(machineList.get(machineIndex));
                // Notice that we leave the PlanningVariable properties on null
                processAssignmentList.add(processAssignment);
                processAssignmentId++;
            }
            machineReassignment.setProcessAssignmentList(processAssignmentList);
        }

        private String readOriginalProcessAssignmentLine() {
            String inputFileName = inputFile.getName();
            String inputFilePrefix = "model_";
            if (!inputFileName.startsWith(inputFilePrefix)) {
                throw new IllegalArgumentException("The inputFile (" + inputFile
                        + ") is expected to start with \"" + inputFilePrefix + "\".");
            }
            File assignmentInputFile = new File(inputFile.getParent(),
                    inputFileName.replaceFirst(inputFilePrefix, "assignment_"));
            BufferedReader assignmentBufferedReader = null;
            try {
                assignmentBufferedReader = new BufferedReader(new FileReader(assignmentInputFile));
                try {
                    return assignmentBufferedReader.readLine();
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Exception in assignmentInputFile ("
                            + assignmentInputFile + ")", e);
                } catch (IllegalStateException e) {
                    throw new IllegalStateException("Exception in assignmentInputFile ("
                            + assignmentInputFile + ")", e);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not read the file (" + assignmentInputFile.getName() + ").", e);
            } finally {
                IOUtils.closeQuietly(assignmentBufferedReader);
            }
        }

    }

}
