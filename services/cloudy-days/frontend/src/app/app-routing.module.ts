import {NgModule} from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {DropletsComponent} from "./droplets/droplets.component";
import {DropletCreateComponent} from "./droplet-create/droplet-create.component";
import {DropletDetailComponent} from "./droplet-detail/droplet-detail.component";

const routes: Routes = [
  {path: '', redirectTo: '/droplets', pathMatch: 'full'},
  {path: 'droplets', component: DropletsComponent},
  {path: 'droplets/:name', component: DropletDetailComponent},
  {path: 'create', component: DropletCreateComponent}
];

@NgModule({
  declarations: [],
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
