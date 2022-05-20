import {NgModule} from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {BeaconsComponent} from "./beacons/beacons.component";
import {BeaconDetailComponent} from "./beacon-detail/beacon-detail.component";

const routes: Routes = [
  {path: '', redirectTo: '/beacons', pathMatch: 'full'},
  {path: 'beacons', component: BeaconsComponent},
  {path: 'beacons/:id', component: BeaconDetailComponent}
];

@NgModule({
  declarations: [],
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
