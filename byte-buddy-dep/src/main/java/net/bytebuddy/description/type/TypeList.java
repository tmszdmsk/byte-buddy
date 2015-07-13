package net.bytebuddy.description.type;

import net.bytebuddy.description.type.generic.GenericTypeDescription;
import net.bytebuddy.description.type.generic.GenericTypeList;
import net.bytebuddy.implementation.bytecode.StackSize;
import net.bytebuddy.matcher.FilterableList;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementations represent a list of type descriptions.
 */
public interface TypeList extends FilterableList<TypeDescription, TypeList> {

    /**
     * Returns a list of internal names of all types represented by this list.
     *
     * @return An array of all internal names or {@code null} if the list is empty.
     */
    String[] toInternalNames();

    /**
     * Returns the sum of the size of all types contained in this list.
     *
     * @return The sum of the size of all types contained in this list.
     */
    int getStackSize();

    /**
     * Represents this list of types into a list of generic types. Invoking this method does not transform types, i.e.
     * no generic information is attached.
     *
     * @return This list of types represented as generic types.
     */
    GenericTypeList asGenericTypes();

    /**
     * Transforms the types of this list by applying the supplied visitor.
     *
     * @param visitor The visitor to apply to each type.
     * @return This type list with all types transformed by the supplied visitor.
     */
    TypeList accept(GenericTypeDescription.Visitor<? extends TypeDescription> visitor);

    /**
     * An abstract base implementation of a type list.
     */
    abstract class AbstractBase extends FilterableList.AbstractBase<TypeDescription, TypeList> implements TypeList {

        @Override
        protected TypeList wrap(List<TypeDescription> values) {
            return new Explicit(values);
        }

        @Override
        public TypeList accept(GenericTypeDescription.Visitor<? extends TypeDescription> visitor) {
            List<TypeDescription> visited = new ArrayList<TypeDescription>(size());
            for (TypeDescription typeDescription : this) {
                visited.add(typeDescription.accept(visitor));
            }
            return new Explicit(visited);
        }
    }

    /**
     * Implementation of a type list for an array of loaded types.
     */
    class ForLoadedType extends AbstractBase {

        /**
         * The loaded types this type list representedBy.
         */
        private final List<? extends Class<?>> types;

        /**
         * Creates a new type list for an array of loaded types.
         *
         * @param type The types to be represented by this list.
         */
        public ForLoadedType(Class<?>... type) {
            this(Arrays.asList(type));
        }

        /**
         * Creates a new type list for an array of loaded types.
         *
         * @param types The types to be represented by this list.
         */
        public ForLoadedType(List<? extends Class<?>> types) {
            this.types = types;
        }

        @Override
        public TypeDescription get(int index) {
            return new TypeDescription.ForLoadedType(types.get(index));
        }

        @Override
        public int size() {
            return types.size();
        }

        @Override
        public String[] toInternalNames() {
            String[] internalNames = new String[types.size()];
            int i = 0;
            for (Class<?> type : types) {
                internalNames[i++] = Type.getInternalName(type);
            }
            return internalNames.length == 0 ? null : internalNames;
        }

        @Override
        public int getStackSize() {
            return StackSize.sizeOf(types);
        }

        @Override
        public GenericTypeList asGenericTypes() {
            return new GenericTypeList.ForLoadedType(types);
        }
    }

    /**
     * A wrapper implementation of an explicit list of types.
     */
    class Explicit extends AbstractBase {

        /**
         * The list of type descriptions this list representedBy.
         */
        private final List<? extends TypeDescription> typeDescriptions;

        /**
         * Creates an immutable wrapper.
         *
         * @param typeDescriptions The list of types to be represented by this wrapper.
         */
        public Explicit(List<? extends TypeDescription> typeDescriptions) {
            this.typeDescriptions = typeDescriptions;
        }

        @Override
        public TypeDescription get(int index) {
            return typeDescriptions.get(index);
        }

        @Override
        public int size() {
            return typeDescriptions.size();
        }

        @Override
        public String[] toInternalNames() {
            String[] internalNames = new String[typeDescriptions.size()];
            int i = 0;
            for (TypeDescription typeDescription : typeDescriptions) {
                internalNames[i++] = typeDescription.getInternalName();
            }
            return internalNames.length == 0 ? null : internalNames;
        }

        @Override
        public int getStackSize() {
            int stackSize = 0;
            for (TypeDescription typeDescription : typeDescriptions) {
                stackSize += typeDescription.getStackSize().getSize();
            }
            return stackSize;
        }

        @Override
        public GenericTypeList asGenericTypes() {
            return new GenericTypeList.Explicit(typeDescriptions);
        }
    }

    /**
     * An implementation of an empty type list.
     */
    class Empty extends FilterableList.Empty<TypeDescription, TypeList> implements TypeList {

        @Override
        public String[] toInternalNames() {
            return null;
        }

        @Override
        public int getStackSize() {
            return 0;
        }

        @Override
        public GenericTypeList asGenericTypes() {
            return new GenericTypeList.Empty();
        }

        @Override
        public TypeList accept(GenericTypeDescription.Visitor<? extends TypeDescription> visitor) {
            return this;
        }
    }
}
