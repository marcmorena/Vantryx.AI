import { Component, Input, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService } from '../../services/category.service';
import { SupplierService } from '../../services/supplier.service';
import { ProductService } from '../../services/product.service';
import { InventoryService } from '../../services/inventory.service';
import { MovementType, StockMovementRequestDTO } from '../../models/movement.model';

@Component({
  selector: 'app-inventario',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventario.component.html',
  styleUrls: ['./inventario.component.css']
})
export class InventarioComponent {
  // Datos que vienen del Padre (AppComponent)
  @Input() productos: any[] = [];
  @Input() categorias: any[] = [];
  @Input() proveedores: any[] = [];
  @Input() usuarioActivo: any = null;

  @Output() productoModificado = new EventEmitter<void>();

  // Variables de control local (Filtros)
  public textoBusqueda: string = '';
  public filtroCategoria: number | null = null;

  // Variables para el Modal de Ajuste Rápido
  public mostrarModalAjuste: boolean = false;
  public productoSeleccionado: any = null;
  public cantidadAjuste: number = 0;
  public motivoAjuste: string = 'Venta';

  public mostrarModalProducto: boolean = false;
  public editandoProducto: boolean = false;
  public idProductoEnEdicion: number | null = null;

  //Categorías
  public mostrandoFormCategoria: boolean = false;
  public nombreNuevaCategoria: string = '';
  public categoriaFiltroId: number | null = null;

  // Usa el tipado correcto si lo tienes importado (ej. public nuevoProducto: Product | any)
public nuevoProducto: any = {
  name: '',
  sku: '',
  description: '',
  salePrice: 0,        
  purchasePrice: 0,    
  leadTime: 1,         
  currentStock: 0,     
  minStock: 0,         
  categoryId: null,  
  supplierId: null     
};

abrirModalCrearProducto() {
  this.editandoProducto = false;
  this.idProductoEnEdicion = null;
  
  // Limpiamos la variable usando tus nombres exactos
  this.nuevoProducto = { 
    name: '', 
    description: '', 
    salePrice: 0, 
    purchasePrice: 0,
    currentStock: 0,
    minStock: 0,
    leadTime: 0,
    categoryId: null,
    supplierId: null
  };
  
  this.mostrarModalProducto = true;
}

  // --- 4. MÉTODOS DE CARGA ---
  
  // Si tu método se llamaba diferente (ej: getProducts), cámbiale el nombre aquí
  cargarProductos() {
    this.productService.getAllProducts().subscribe(data => this.productos = data);
  }

 cargarCategorias() {
  this.categoryService.getAll().subscribe(res => {
    this.categorias = res; 
    // Esto asegura que el selector HTML vea la nueva lista inmediatamente
  });
}

  cargarProveedores() {
    this.supplierService.getAll().subscribe(data => this.proveedores = data);
  }

  // EL CONSTRUCTOR FUSIONADO
  constructor(
    private inventoryService: InventoryService,
    private productService: ProductService,
    private categoryService: CategoryService,
    private supplierService: SupplierService,
    private cdr: ChangeDetectorRef
    ) {}


ngOnInit(): void {
  // 1. Cargas iniciales
  this.cargarCategorias();
  this.cargarProveedores();

  // 2. Escuchamos el cambio de categoría
  this.categoryService.categoriaSeleccionada$.subscribe(id => {
    this.filtroCategoria = id; 

    // 3. El secreto está aquí:
    setTimeout(() => {
      this.productService.getAllProducts().subscribe(data => {
        // 🔥 TRUCO MAESTRO:
        // No hacemos solo "this.productos = data". 
        // Al usar [...data], creamos una COPIA nueva. 
        // Angular nota que la dirección de memoria cambió y refresca el HTML.
        this.productos = [...data]; 
        
        // Forzamos a Angular a que examine la vista ahora mismo
        this.cdr.detectChanges();
        
      });
    }, 300); 
  });
  
  this.categoryService.refreshNeeded$.subscribe(() => {
    this.cargarCategorias();
  });
}

  get productosFiltrados() {
  return this.productos.filter(p => {
    // 1. Filtro de búsqueda (usando textoBusqueda)
    const busqueda = this.textoBusqueda ? this.textoBusqueda.toLowerCase() : '';
    const coincideTexto = p.name.toLowerCase().includes(busqueda);
    
    // 2. Filtro de Categoría (usando filtroCategoria)
    // Si filtroCategoria es null, devuelve true (no filtra). 
    const coincideCategoria = this.filtroCategoria 
      ? (p.categoryId === this.filtroCategoria || (p.category && p.category.id === this.filtroCategoria))
      : true;

    return coincideTexto && coincideCategoria;
  });
}

  // Función para el botón verde de "Nuevo Producto"
prepararNuevoProducto() {
  this.editandoProducto = false;
  this.resetNuevoProducto(); // Esto limpia los campos
  this.mostrarModalProducto = true;
}

  // Añade esto al final si no lo tienes:
  cargando: boolean = false;
guardarProducto() {
  // 1. Verificación robusta
  if (!this.nuevoProducto.name || !this.nuevoProducto.categoryId) {
    return;
  }

  // 2. Formateo de datos (IDs y números asegurados)
  const productoDTO = {
    ...this.nuevoProducto,
    categoryId: Number(this.nuevoProducto.categoryId),
    supplierId: this.nuevoProducto.supplierId ? Number(this.nuevoProducto.supplierId) : null,
    // Aseguramos que los precios y stocks viajen como números por si el input los dejó como string
    salePrice: Number(this.nuevoProducto.salePrice || 0),
    purchasePrice: Number(this.nuevoProducto.purchasePrice || 0),
    leadTime: Number(this.nuevoProducto.leadTime || 1),
    currentStock: Number(this.nuevoProducto.currentStock || 0),
    minStock: Number(this.nuevoProducto.minStock || 0)
  };

  // 3. Decidir si EDITAR o CREAR
  if (this.editandoProducto && productoDTO.id) {
    // --- LÓGICA DE EDICIÓN (PUT) ---
    this.productService.updateProduct(productoDTO.id, productoDTO).subscribe({
      next: (res) => this.finalizarOperacion('actualizado'),
      error: (err) => this.manejarError(err)
    });
  } else {
    // --- LÓGICA DE CREACIÓN (POST) ---
    this.productService.save(productoDTO).subscribe({
      next: (res) => this.finalizarOperacion('creado'),
      error: (err) => this.manejarError(err)
    });
  }
}

// --- Funciones auxiliares para mantener el código limpio ---

private finalizarOperacion(_accion: string) {
  
  this.mostrarModalProducto = false;
  this.editandoProducto = false; // Importante resetear este flag
  this.resetNuevoProducto(); 

  // Avisamos y refrescamos
  this.productoModificado.emit(); 
  this.cargarProductos();
}

private manejarError(err: any) {
  if(err.status === 403) {
    alert('No tienes permisos para realizar esta acción.');
  } else {
    alert('Error al procesar el producto. Revisa la consola.');
  }
}
resetNuevoProducto() {
  this.nuevoProducto = {
    name: '',
    sku: '',
    description: '',
    salePrice: 0,
    purchasePrice: 0,
    leadTime: 1,
    currentStock: 0,
    minStock: 0,
    categoryId: null,
    supplierId: null
  };
}

  // --- MÉTODOS DE AJUSTE DE STOCK ---

  ajusteRapido(producto: any, cantidad: number) {
  const nuevoStock = producto.currentStock + cantidad;
  
  if (nuevoStock < 0) return; // Evitar stock negativo

  // 1. Actualizamos el valor visual
  producto.currentStock = nuevoStock;

  // 2. Registramos el movimiento (Kardex Silencioso)
  const log = {
    fecha: new Date(),
    producto: producto.name,
    variacion: cantidad,
    stockFinal: nuevoStock,
    usuario: this.usuarioActivo?.nombre || 'Sistema',
    motivo: cantidad > 0 ? 'Entrada rápida' : 'Salida rápida'
  };

  // Aquí podrías llamar a un servicio para guardar en BD
  // this.productService.updateStock(producto.id, nuevoStock, log).subscribe();
  }

  abrirAjuste(producto: any) {
    this.productoSeleccionado = producto;
    this.cantidadAjuste = 0;
    this.motivoAjuste = 'Venta';
    this.mostrarModalAjuste = true;
  }

  guardarMovimiento() {
  if (!this.productoSeleccionado || this.cantidadAjuste === 0) return;

  const nuevoStock = this.productoSeleccionado.currentStock + this.cantidadAjuste;
  
  if (nuevoStock < 0) {
    alert('⚠️ Error: No puedes retirar más stock del que existe.');
    return;
  }

  // Sincronizamos con el Enum de Java: IN, OUT o ADJUSTMENT
  let tipoFinal: MovementType;
  if (this.motivoAjuste === 'Ajuste Inventario') {
    tipoFinal = 'ADJUSTMENT';
  } else {
    tipoFinal = this.cantidadAjuste > 0 ? 'IN' : 'OUT';
  }

  // Creamos el DTO tipado para Spring Boot
  const movimientoKardex: StockMovementRequestDTO = {
    productId: this.productoSeleccionado.id,
    quantity: Math.abs(this.cantidadAjuste),
    type: tipoFinal,
    reason: this.motivoAjuste
  };

  this.inventoryService.registrarMovimiento(movimientoKardex).subscribe({
    next: () => {
      this.productoSeleccionado.currentStock = nuevoStock;
      this.mostrarModalAjuste = false;
      this.cantidadAjuste = 0;
      this.productoModificado.emit(); // Notifica al dashboard para que recalcule métricas
      this.cdr.detectChanges();
    },
    error: (err) => {
      if (err.status === 403 || err.status === 401) {
        alert('🚫 Error de Seguridad: No tienes permisos (ADMIN) o falta el token.');
      } else {
        alert('❌ Error al conectar con el servidor. Revisa si el backend está activo.');
      }
    }
  });
}

  getNombreProveedor(id: number) {
    return this.proveedores.find(s => s.id === id)?.name || 'N/A';
  }

  eliminarProducto(id: number) {
  if (confirm('¿Eliminar producto?')) {
    this.productService.deleteProduct(id).subscribe({
      next: () => {
        // ✅ SOLO modificamos la fuente. El getter hará el resto.
        this.productos = this.productos.filter(p => p.id !== id);
        this.productoModificado.emit();
      }
    });
  }
}

abrirModalEditar(productoSeleccionado: any) {
  this.editandoProducto = true;
  // Hacemos una copia para que los cambios no se vean en la tabla 
  // hasta que el servidor diga "OK"
  this.nuevoProducto = { ...productoSeleccionado }; 
  this.mostrarModalProducto = true;
}

// Método para guardar la categoría rápidamente
crearNuevaCategoria() {
  if (!this.nombreNuevaCategoria.trim()) return;

  const nuevaCat = { name: this.nombreNuevaCategoria };

  this.categoryService.save(nuevaCat).subscribe({
    next: (catGuardada) => {
      // 1. La añadimos a nuestra lista local para que aparezca en el select
      this.categorias.push(catGuardada);
      // 2. La seleccionamos automáticamente en el producto que estamos creando
      this.nuevoProducto.categoryId = catGuardada.id;
      // 3. Limpiamos y cerramos el mini-formulario
      this.nombreNuevaCategoria = '';
      this.mostrandoFormCategoria = false;
    },
    error: (err) => alert('Error al crear la categoría')
  });
}

sincronizarFiltro() {
  // Le avisamos al servicio del nuevo cambio manual
  this.categoryService.setCategoriaSeleccionada(this.filtroCategoria);
}

}
