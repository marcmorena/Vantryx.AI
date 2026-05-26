import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { LoginComponent } from './components/login/login.component';
import { InventarioComponent } from './components/inventario/inventario.component';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { ProductService } from './services/product.service';
import { CategoryService } from './services/category.service';
import { SupplierService } from './services/supplier.service';
import { CategoryDTO } from './models/category.model';
import { Supplier } from './models/supplier.model';
import { ProductDTO } from './models/product.model';
import { FormsModule } from '@angular/forms';
import { ProveedoresComponent } from './components/proveedores/proveedores.component';
import { ComprasComponent } from './components/compras/compras.component';
import { PurchaseOrderModalComponent } from './components/purchase-order-modal/purchase-order-modal.component';
import { MovimientosComponent } from './components/movimientos/movimientos.component';
import { VentasComponent } from './components/ventas/ventas.component';
import { ReportesComponent } from './components/reportes/reportes.component';
import { AsistenteIaComponent } from './components/asistente-ia/asistente-ia.component';
import { SaleService } from './services/sale.service';
import { DashboardStatsDTO } from './models/sale.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [LoginComponent, InventarioComponent, ProveedoresComponent, ComprasComponent, PurchaseOrderModalComponent, MovimientosComponent, VentasComponent, ReportesComponent, AsistenteIaComponent, CommonModule, CurrencyPipe, DatePipe, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private supplierService = inject(SupplierService);
  private saleService = inject(SaleService);
  private cdr = inject(ChangeDetectorRef);

  // ESTADO GLOBAL
  public vistaActual: 'dashboard' | 'productos' | 'proveedores' | 'compras' | 'ventas' | 'movimientos' | 'reportes' = 'dashboard';
  public panelIaAbierto = false;
  public isLoading = true;
  public isLogged: boolean = false;
  public usuarioActivo: any = null;

  // FUENTE DE DATOS (Source of Truth)
  public products: any[] = [];
  public categories: CategoryDTO[] = [];
  public suppliers: Supplier[] = [];
  
  // VARIABLE PARA PASAR FILTRO AL HIJO
  public filtroExternoCategoria: number | null = null;

  public dashboardData = {
    totalRevenue: 0,
    lowStockProducts: [] as any[]
  };

  public dashboardCargando = false;
  public financialStats: DashboardStatsDTO | null = null; // Para Reportes y Ventas

  public modalReponerAbierto = false;
  public productoParaReponer: ProductDTO | null = null;

  ngOnInit() {
    // Solo nos suscribimos a los observables de servicio; los datos se cargan tras el login
    this.categoryService.refreshNeeded$.subscribe(() => {
      this.cargarCategorias();
    });
    this.categoryService.categoriaSeleccionada$.subscribe(id => {
      this.filtroExternoCategoria = id;
    });
  }
  

  // GESTIÓN DE DATOS
  cargarDashboard() {
    this.dashboardCargando = true;
    this.productService.getAllProducts().subscribe({
      next: (data) => {
        this.products = data;

        const revenue = data.reduce((acc, p) => acc + (Number(p.salePrice || 0) * Number(p.currentStock || 0)), 0);
        const alertas = data.filter(p => (p.currentStock || 0) <= (p.minStock || 0));

        this.dashboardData = {
          totalRevenue: revenue,
          lowStockProducts: alertas
        };

        this.dashboardCargando = false;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando productos:', err);
        this.dashboardCargando = false;
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  cargarStats() {
    this.saleService.getStats().subscribe({
      next: (data) => {
        this.financialStats = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error cargando stats financieros:', err)
    });
  }

  cargarListasAuxiliares() {
    this.categoryService.getAll().subscribe(data => {
      this.categories = data;
      this.cdr.detectChanges();
    });
    this.supplierService.getAll().subscribe(data => {
      this.suppliers = data;
      this.cdr.detectChanges();
    });
  }

  // En app.component.ts

irACategoria(categoriaId: number | undefined) {
  if (categoriaId === undefined) return;

  // 1. Actualizamos nuestra variable local (la que ahora coincide con el HTML)
  this.filtroExternoCategoria = categoriaId; 

  // 2. Avisamos al servicio para que el Inventario se filtre
  this.categoryService.setCategoriaSeleccionada(categoriaId); 

  // 3. Navegación
  this.vistaActual = 'productos';
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

cargarCategorias() {
  this.categoryService.getAll().subscribe({
    next: (data) => {
      this.categories = data;
      console.log('Categorías cargadas en el Dashboard:', this.categories.length);
      this.cdr.detectChanges();
    },
    error: (err) => console.error('Error al cargar categorías en Dashboard', err)
  });
}

// app.component.ts

confirmarEliminarCategoria(event: Event, catABorrar: any) {
  // 1. Detenemos el evento para que no se dispare el clic de la tarjeta
  event.stopPropagation();

  // 2. Buscamos la categoría de respaldo
  const catGeneral = this.categories.find(c => c.name.toLowerCase() === 'general');

  if (!catGeneral) {
    alert("Error: Crea una categoría llamada 'General' para poder mover los productos.");
    return;
  }

  if (catABorrar.id === catGeneral.id) {
    alert("No puedes borrar la categoría de respaldo.");
    return;
  }

  const confirmacion = confirm(
    `¿Borrar "${catABorrar.name}"?\nLos productos se moverán a "${catGeneral.name}".`
  );

  if (confirmacion) {
    this.categoryService.deleteAndMove(catABorrar.id, catGeneral.id).subscribe({
      next: () => {

        this.categories = [...this.categories.filter(c => c.id !== catABorrar.id)];
        this.cdr.detectChanges();

        this.categoryService.setCategoriaSeleccionada(catGeneral.id);

        setTimeout(() => {
  
          this.vistaActual = 'productos'; 
          
          console.log('Navegando a General con datos actualizados');
        }, 300);

        this.cargarCategorias();
      },
      error: (err) => {
        console.error('Error:', err);
        alert('No se pudo eliminar la categoría.');
      }
    });
  }
}

  abrirModalReponer(prod: ProductDTO) {
    this.productoParaReponer = prod;
    this.modalReponerAbierto = true;
  }

  cerrarModalReponer() {
    this.modalReponerAbierto = false;
    this.productoParaReponer = null;
  }

  onOrdenCreadaDesdeReponer() {
    this.cerrarModalReponer();
    this.cargarDashboard();
  }

  manejarLogin(usuario: any) {
    this.isLogged = true;
    this.usuarioActivo = usuario;
    this.vistaActual = 'dashboard';
    // Cargamos datos ahora que ya hay JWT válido en localStorage
    this.cargarDashboard();
    this.cargarListasAuxiliares();
    this.cargarCategorias();
    this.cargarStats();
  }

  getCountByCategoria(categoriaId: number): number {
    return this.products.filter(p => p.categoryId === categoriaId).length;
  }
}