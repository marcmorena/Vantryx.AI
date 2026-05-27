import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Supplier } from '../../models/supplier.model';
import { SupplierService } from '../../services/supplier.service';

@Component({
  selector: 'app-proveedores',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './proveedores.component.html',
  styleUrls: ['./proveedores.component.css']
})
export class ProveedoresComponent implements OnInit {
  public suppliers: Supplier[] = [];
  public mostrarModalProveedor = false;
  public editando = false;
  public idProveedorEnEdicion: number | null = null;

  // Inicializamos el objeto para evitar errores de "undefined" en el HTML
  public nuevoProveedor: Supplier = {
    id: 0,
    name: '',
    contactName: '',
    email: '',
    phone: '',
    address: ''
  };

  constructor(
    private supplierService: SupplierService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
  // Cargamos al iniciar, pero con una pequeña pausa para asegurar que el componente esté listo
  setTimeout(() => this.cargarProveedores(), 100);
}

  cargarProveedores() {
  this.supplierService.getAll().subscribe({
    next: (data) => {
      // Limpiamos cualquier residuo y asignamos los datos reales del servidor
      this.suppliers = [...data]; 
      this.cdr.detectChanges(); // Forzamos a Angular a pintar la lista
    },
    error: () => {}
  });
}

  abrirModalCrear() {
    this.editando = false;
    this.nuevoProveedor = { id: 0, name: '', contactName: '', email: '', phone: '', address: '' };
    this.mostrarModalProveedor = true;
  }

  prepararEdicion(sup: Supplier) {
    this.editando = true;
    this.idProveedorEnEdicion = sup.id!;
    this.nuevoProveedor = { ...sup }; 
    this.mostrarModalProveedor = true;
  }

  guardarProveedor() {
  // Validación básica para evitar tarjetas vacías
  if (!this.nuevoProveedor.name || this.nuevoProveedor.name.trim() === '') {
    alert('El nombre de la empresa es obligatorio');
    return;
  }

  if (this.editando && this.idProveedorEnEdicion) {
    this.supplierService.update(this.idProveedorEnEdicion, this.nuevoProveedor).subscribe({
      next: () => this.finalizarAccionProveedor(),
      error: () => alert('Error al actualizar')
    });
  } else {
    this.supplierService.save(this.nuevoProveedor).subscribe({
      next: (res) => {
        // IMPORTANTE: No hagas "push" manual. 
        // Deja que finalizarAccionProveedor refresque la lista desde la DB.
        this.finalizarAccionProveedor();
      },
      error: () => alert('Error al guardar')
    });
  }
}

  private finalizarAccionProveedor() {
  this.mostrarModalProveedor = false;
  this.editando = false;
  this.idProveedorEnEdicion = null;
  
  // REINICIO TOTAL del objeto para que no queden datos vinculados a una tarjeta vacía
  this.nuevoProveedor = { id: 0, name: '', contactName: '', email: '', phone: '', address: '' };
  
  // Recargamos la lista oficial desde el servidor
  this.cargarProveedores();
}

  eliminarProveedor(id: any, event: Event) {
    event.preventDefault();
    event.stopPropagation();
    
    if (!id || !confirm('¿Eliminar este proveedor de forma definitiva?')) return;

    const idABorrar = Number(id);
    this.suppliers = this.suppliers.filter(s => s.id !== idABorrar);
    this.cdr.detectChanges();

    this.supplierService.delete(idABorrar).subscribe({
      next: () => {},
      error: (err) => {
        alert('No se pudo borrar: El proveedor tiene productos vinculados.');
        this.cargarProveedores();
      }
    });
  }

  trackById(index: number, item: Supplier) {
  return item.id; // Ayuda a Angular a no confundir las tarjetas
}
}
